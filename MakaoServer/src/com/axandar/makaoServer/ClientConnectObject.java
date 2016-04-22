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

/**
 * Created by Axandar on 06.02.2016.
 */
public class ClientConnectObject implements Runnable {

    // TODO: 22.03.2016 Add option for call "stop makao" on another player

    private Socket socket;
    private int id;
    private Connection conn;

    private SessionInfo sessionInfo;
    private TableServer table;
    private Player threadPlayer;

    private String TAG = "Server";

    public ClientConnectObject(Socket _socket, int _id, SessionInfo _sessionInfo){
        socket = _socket;
        id =_id;
        sessionInfo = _sessionInfo;
    }

    @Override
    public void run() {
        String playerIp = socket.getInetAddress().toString();
        TAG += " " + playerIp;
        Logger.logConsole(TAG, "Player connected");

        if(initStreams()){
            settingUpPlayer(playerIp);
            waitForGameStart();
            table = sessionInfo.getTable();

            send(ServerProtocol.GAME_STARTED);
            send(threadPlayer);
            for(Player player:sessionInfo.getPlayersObjectsInOrder()){
                send(player);
            }
            send(sessionInfo.getCardOnTop());

            while(!sessionInfo.isGameExiting()){
                runningGame();
                /**if(sessionInfo.getPlayers().size() == 1){
                 sessionInfo.setGameExited(true);
                 }**/// TODO: 25.03.2016 Stay for debug only
            }

            closeSockets();
        }
    }

    private boolean initStreams(){
        try{
            conn = new Connection(new ObjectInputStream(socket.getInputStream()),
                    new ObjectOutputStream(socket.getOutputStream()));
            return true;
        }catch(IOException e){
            Logger.logError(e);
            return false;
        }
    }

    private void settingUpPlayer(String playerIp){
        Object received = receive();
        if(received instanceof String){
            threadPlayer = new Player((String)received, playerIp, id);
            sessionInfo.addPlayerObjectToList(threadPlayer);
            send(sessionInfo.getPlayersObjectsInOrder().size());
            sessionInfo.decreasePlayersNotReady();
        }
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

    private void runningGame() {
        //rest players ending theirs turns
        while((!sessionInfo.getActualTurnPlayer().equals(threadPlayer))){
            Logger.logConsole(TAG, "Handle another player turn");
            waitForNextPlayerEndTurn();
            handleAnotherPlayersTurns();

            if(sessionInfo.getLastSaid().getToWho().equals(threadPlayer) && threadPlayer.getCardsInHand().size() == 1
                    && !threadPlayer.isMakao()){
                playerGetCards(5);
            }

            checkForMakao();
            // TODO: 22.04.2016 check if player wanted to say makao
        }
        Logger.logConsole(TAG, "Player started turn");
        turnStarted();
        table.endTurn(threadPlayer);
        Logger.logConsole(TAG, "Player ended turn");
    }

    private void waitForNextPlayerEndTurn(){
        Player playerEndedTurnCache = sessionInfo.getLastTurnEndedPlayer();
        while(playerEndedTurnCache.equals(sessionInfo.getLastTurnEndedPlayer())){
            try{
                wait(1000);
            }catch(InterruptedException e){
                Logger.logError(e);
            }
        }
    }

    private void handleAnotherPlayersTurns(){
        send(ServerProtocol.START_UPDATE);
        for(Player player:sessionInfo.getPlayersObjectsInOrder()){
            send(player);
        }
        for(Card card:sessionInfo.getLastPlacedCards()){
            send(card);
        }
        send(ServerProtocol.STOP_UPDATE);
    }

    private void checkForMakao(){
        send(ServerProtocol.IS_SAID_STOPMAKAO);
        Object received = receive();
        if(received instanceof Integer && (int)received == ServerProtocol.PLAYER_SAID_STOPMAKAO){
            received = receive();
            sessionInfo.setLastSaid((StopMakao)received);
        }
    }

    private void turnStarted(){
        send(ServerProtocol.TURN_STARTED);
        turnProcessing();

        if(!threadPlayer.wasPuttedCard()){
            playerNotPuttedCard();
        }

        if(threadPlayer.getCardsInHand().size() == 0){
            playerEndedGame();
        }
        Logger.logConsole(TAG, "Updating player data");
        send(threadPlayer);
        table.endTurn(threadPlayer);
    }

    private void turnProcessing(){
        threadPlayer.setWasPuttedCard(false);
        int command = -1;
        while(isTurnNotEnded(command)){
            Object received = receive();
            if(received instanceof Card){
                receivedCard((Card) received);
            }else if(received instanceof Integer){
                command = (int) received;
            }

            if(command == ServerProtocol.PLAYER_SET_MAKAO && threadPlayer.getCardsInHand().size() == 1){
                threadPlayer.setMakao(true);
            }
        }
    }

    private boolean isTurnNotEnded(int intToCheck){
        return !(intToCheck == ServerProtocol.TURN_ENDED || intToCheck == ServerProtocol.PLAYER_SET_MAKAO);
    }

    private void receivedCard(Card card){
        Logger.logConsole(TAG, "Server received card: " + card.getIdType() + "-" + card.getIdColor());
        if(isOrderCard(card)){
            gotOrderCard(card);
        }else{
            gotNormalCard(card);
        }
    }

    private void gotOrderCard(Card card){
        send(ServerProtocol.GOT_ORDER_CARD);
        getOrderedCard(card);
    }

    private void getOrderedCard(Card orderingCard){
        Object received = receive();
        send(ServerProtocol.GOT_ORDERED_CARD);
        if(received instanceof Card){
            Card orderedCard = (Card) received;
            if(table.putOrderCardOnTable(orderingCard, orderedCard)){
                send(ServerProtocol.CARD_ACCEPTED);
                threadPlayer.removeCardFromHand(orderingCard);
                threadPlayer.setWasPuttedCard(true);
            }else{
                send(ServerProtocol.CARD_NOTACCEPTED);
                threadPlayer.setWasPuttedCard(false);
            }
        }
    }

    private void gotNormalCard(Card card){
        send(ServerProtocol.GOT_CARD);
        if(table.putCardOnTable(card)){
            Logger.logConsole(TAG, "Received card accepted");
            send(ServerProtocol.CARD_ACCEPTED);
            threadPlayer.removeCardFromHand(card);
            threadPlayer.setWasPuttedCard(true);
        }else{
            Logger.logConsole(TAG, "Received card not accepted");
            send(ServerProtocol.CARD_NOTACCEPTED);
            threadPlayer.setWasPuttedCard(false);
        }
    }

    private boolean isOrderCard(Card card){
        return card.getFunction().getFunctionID() == Function.ORDER_CARD
                || card.getFunction().getFunctionID() == Function.CHANGE_COLOR;
    }

    private void playerNotPuttedCard(){
        Logger.logConsole(TAG, "Player did not put card");
        if(sessionInfo.getQuantityCardsToTake() > 0){
            playerGetCards(sessionInfo.getQuantityCardsToTake());
        }else if(sessionInfo.getQuantityTurnsToWait() > 0){
            playerWaitTurns();
        }else{
            playerGetCard();
        }
    }

    private void playerGetCards(int numberOfCards){
        //probably not needed returning object
        threadPlayer = table.giveCardToPlayer(threadPlayer, numberOfCards);
        sessionInfo.setQuantityCardsToTake(0);
        threadPlayer.setMakao(false);
    }

    private void playerWaitTurns(){
        threadPlayer = table.setPlayerToWaitTurns(threadPlayer, sessionInfo.getQuantityTurnsToWait());
        sessionInfo.setQuantityTurnsToWait(0);
    }

    private void playerGetCard(){
        threadPlayer = table.giveCardToPlayer(threadPlayer, 1);
        threadPlayer.setMakao(false);
    }

    private void playerEndedGame(){
        send(ServerProtocol.GAME_ENDED);
        sessionInfo.removePlayerFromList(threadPlayer);
    }

    private void closeSockets() {
        Logger.logConsole(TAG, "Closing client socket");
        conn.close();
        try{
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void send(Object object){
        conn.send(object);
    }

    private Object receive(){
        return conn.receive();
    }
}