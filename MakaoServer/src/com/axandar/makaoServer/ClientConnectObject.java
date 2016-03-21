package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;
import com.axandar.makaoCore.utils.ServerProtocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by Axandar on 06.02.2016.
 */
public class ClientConnectObject implements Runnable {

    // TODO: 22.03.2016 Add option for call "stop makao" on another player

    private String TAG = "Server";

    private Socket socket;
    private int id;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;

    private SessionInfo sessionInfo;
    private TableServer table;
    private Player threadPlayer;

    public ClientConnectObject(Socket _socket, int _id, SessionInfo _sessionInfo, TableServer _table){
        socket = _socket;
        id =_id;
        sessionInfo = _sessionInfo;
        table = _table;
    }


    @Override
    public void run() {
        String playerIp = socket.getInetAddress().toString();
        TAG += " " + playerIp;
        Logger.logConsole(TAG, playerIp);

        try {
            initStreams();
            settingUpPlayer(playerIp);
            waitForGameStart();
            outputStream.writeObject(ServerProtocol.GAME_STARTED);
            outputStream.writeObject(threadPlayer);//aktualizacja kart w reku

            sendUpdatedPlayersInformation(sessionInfo.getPlayers());
            while(!sessionInfo.isGameExited()){
                runningGame();
                if(sessionInfo.getPlayers().size() == 1){
                    sessionInfo.setGameExited(true);
                }
            }

            closeSockets();
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            Logger.logError(e);
        }
    }

    private void runningGame() throws IOException, InterruptedException, ClassNotFoundException{
        //rest players ending theirs turns
        while(!(sessionInfo.getJustEndedTurnPlayerId() == threadPlayer.getPlayerID())){
            handleAnotherPlayersTurns();
        }
        waitForTurn(); // TODO: 08.03.2016 is needed?
        turnStarted();
        table.endTurn(threadPlayer);
    }

    private void initStreams() throws IOException{
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    private void settingUpPlayer(String playerIp) throws IOException, ClassNotFoundException{
        Object receivedFromClient = inputStream.readObject();

        if(receivedFromClient instanceof String){
            String playerName = (String) receivedFromClient;
            Logger.logConsole(TAG, playerName);
            threadPlayer = new Player(playerName, playerIp, id);
            sessionInfo.addPlayer(threadPlayer);

            outputStream.writeObject(ServerProtocol.ACCEPTED_NICK);
            sessionInfo.decreasePlayersNotReady();
        }
    }

    private void handleAnotherPlayersTurns() throws InterruptedException, IOException{
        int savedJustEndedTurnPlayerId = sessionInfo.getJustEndedTurnPlayerId();
        while(savedJustEndedTurnPlayerId == sessionInfo.getJustEndedTurnPlayerId()){
            Thread.sleep(1000);
        }// TODO: 24.02.2016 mozliwy blad // sprawdzic poprawnosc
        sendUpdatedPlayersInformation(sessionInfo.getPlayers());// TODO: 17.03.2016 is needed to send all players informations? 
        sendUpdatedCardOnTop();
    }

    private void sendUpdatedPlayersInformation(List<Player> players) throws IOException{
        // TODO: 22.02.2016 wysylanie zaktualizowanych infomracji o graczach(np. ilosc kart w reku)
        outputStream.writeObject(ServerProtocol.START_UPDATE_PLAYERS);
        for(Player player:players){
            outputStream.writeObject(player);
        }
        outputStream.writeObject(ServerProtocol.END_UPDATE_PLAYERS);
    }

    private void sendUpdatedCardOnTop() throws IOException{
        outputStream.writeObject(table.getCardOnTop());
    }

    private void waitForGameStart(){
        while(!sessionInfo.isGameStarted()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.logError(e);
            }
        }
    }

    private void turnStarted() throws IOException, ClassNotFoundException {
        outputStream.writeObject(ServerProtocol.TURN_STARTED);
        turnProcessing();

        if(!threadPlayer.wasPuttedCard()){
            playerNotPuttedCard();
        }

        if(threadPlayer.getCardsInHand().size() == 0){
            playerEndedGame();
        }

        outputStream.writeObject(threadPlayer);
        table.endTurn(threadPlayer);
    }

    private void turnProcessing() throws IOException, ClassNotFoundException{
        int receivedCommand = 0;
        threadPlayer.setWasPuttedCard(false);
        while(isTurnNotEnded(receivedCommand)){
            Object receivedObject = inputStream.readObject();
            if(receivedObject instanceof Integer){
                outputStream.writeObject(ServerProtocol.GOT_CMD);
                receivedCommand = (int) receivedObject;
            }else if(receivedObject instanceof Card){
                receivedCard((Card) receivedObject);
            }

            isCommandMakao(receivedCommand);
        }
    }

    private boolean isTurnNotEnded(int intToCheck){
        return !(intToCheck == ServerProtocol.TURN_ENDED || intToCheck == ServerProtocol.PLAYER_SET_MAKAO);
    }

    private void receivedCard(Card card) throws IOException, ClassNotFoundException{
        if(isOrderCard(card)){
            gotOrderCard(card);
        }else{
            gotNormalCard(card);
        }
    }

    private void gotOrderCard(Card card) throws IOException, ClassNotFoundException{
        outputStream.writeObject(ServerProtocol.GOT_ORDER_CARD);
        getOrderedCard(card);
    }

    private void getOrderedCard(Card orderingCard) throws IOException, ClassNotFoundException{
        Object receivedObject = inputStream.readObject();
        if(receivedObject instanceof Card){
            Card orderedCard = (Card) receivedObject;
            if(table.putOrderCardOnTable(orderingCard, orderedCard, threadPlayer)){
                outputStream.writeObject(ServerProtocol.CARD_ACCEPTED);
                threadPlayer.setWasPuttedCard(true);
            }else{
                outputStream.writeObject(ServerProtocol.CARD_NOTACCEPTED);
                threadPlayer.setWasPuttedCard(false);
            }
        }
    }

    private void gotNormalCard(Card card) throws IOException{
        outputStream.writeObject(ServerProtocol.GOT_CARD);
        if(table.putCardOnTable(card, threadPlayer)){
            outputStream.writeObject(ServerProtocol.CARD_ACCEPTED);
            threadPlayer.setWasPuttedCard(true);
        }else{
            outputStream.writeObject(ServerProtocol.CARD_NOTACCEPTED);
        }
    }

    private boolean isOrderCard(Card card){
        return card.getFunction().getFunctionID() == Function.ORDER_CARD
                || card.getFunction().getFunctionID() == Function.CHANGE_COLOR;
    }

    private void isCommandMakao(int command){
        if(command == ServerProtocol.PLAYER_SET_MAKAO){
            threadPlayer.setMakao(true);
        }/**else if(command == ServerProtocol.PLAYER_CANCEL_MAKAO){
            threadPlayer.setMakao(false);
        }**/
    }

    private void playerNotPuttedCard(){
        if(table.getQuantityCardsToTake() > 0){
            playerGetCards();
        }else if(table.getQuantityTurnsToWait() > 0){
            playerWaitTurns();
        }else{
            playerGetCard();
        }
    }

    private void playerGetCards(){
        table.giveCardToPlayer(threadPlayer, table.getQuantityCardsToTake());
        table.setQuantityCardsToTake(0);
        threadPlayer.setMakao(false);
    }

    private void playerWaitTurns(){
        table.setPlayerToWaitTurns(threadPlayer, table.getQuantityTurnsToWait());
        table.setQuantityTurnsToWait(0);
    }

    private void playerGetCard(){
        table.giveCardToPlayer(threadPlayer, 1);
        threadPlayer.setMakao(false);
    }

    private void playerEndedGame() throws IOException{
        outputStream.writeObject(ServerProtocol.GAME_ENDED);
        //threadPlayer.setMakao(true);
        sessionInfo.removePlayer(threadPlayer);
    }

    private void waitForTurn(){
        while(!(table.getActualPlayer().getPlayerID() == threadPlayer.getPlayerID())){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.logError(e);
            }
        }
    }

    private void closeSockets() throws IOException{
        Logger.logConsole(TAG, "Closing client socket");
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}