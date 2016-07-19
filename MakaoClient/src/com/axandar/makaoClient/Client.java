package com.axandar.makaoClient;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Connection;
import com.axandar.makaoCore.utils.Logger;
import com.axandar.makaoCore.utils.ServerProtocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 26.01.2016.
 */
public class Client implements Runnable{

    // TODO: 29.04.2016 Tests:
    //1. Makao operation
    //2. Sending many cards
    //4. More players
    //5. Ordering cards

    private String TAG = "Client backend";

    private String ip;
    private int port;
    private String nickname;
    private ClientProperties properties;
    private Connection conn;

    private int command = -1;
    private List<Player> updatedPlayers = new ArrayList<>();

    public Client(ClientProperties _properties){
        properties = _properties;
        ip = properties.getIp();
        port = properties.getPort();
        nickname = properties.getNickname();

    }

    @Override
    public void run(){
        Logger.logConsole(TAG, "Started client backend");
        if(startConnection()){
            Logger.logConsole(TAG, "Started information trading");
            tradingInformation();
        }else{
            Logger.logConsole(TAG, "Error in establishing connection with server");
        }
    }

    private boolean startConnection(){
        try{
            Socket connectionToServer = new Socket(ip, port);
            ObjectOutputStream oos = new ObjectOutputStream(connectionToServer.getOutputStream());
            oos.flush();
            ObjectInputStream is = new ObjectInputStream(connectionToServer.getInputStream());
            conn = new Connection(is, oos);

            properties.setClientRunning(true);
            Logger.logConsole(TAG, "Client fully running");
            return true;
        }catch(IOException e){
            Logger.logError(e);
            return false;
        }
    }

    private void tradingInformation(){
        send(nickname);

        Object received = receive();
        if(received instanceof Integer){
            properties.setEstimatedPlayersNumber((int) received);
        }

        received = receive();
        if(received instanceof Integer && (int) received == ServerProtocol.GAME_STARTED){
            received = receive();
            if(received instanceof Player){
                properties.setLocalPlayer((Player) received);
                TAG = properties.getLocalPlayer().getPlayerName() + " with id: " +
                        properties.getLocalPlayer().getPlayerID() + " " + TAG;
            }

            int i = 1;
            while(i < properties.getEstimatedPlayersNumber()){
                received = receive();
                if(received instanceof Player){
                    i++;
                    properties.addPlayer((Player) received);
                    Logger.logConsole(TAG, "Added player: " + ((Player) received).getPlayerName());
                }
            }
            received = receive();
            while(received instanceof Integer){
                properties.addSuitableCardsToOrder((Integer) received);
                received = receive();
            }

            if(received instanceof Card){
                properties.setCardOnTop((Card) received);
            }
            properties.setUpdateGame(true);
            handleCommands();
        }

    }

    private void handleCommands(){
        while(properties.isClientRunning()){
            Object received = receive();
            if(received instanceof Integer){
                if((int) received == ServerProtocol.START_UPDATE){
                    Logger.logConsole(TAG, "Start update players");
                    updatePlayersInformation();
                }else if((int) received == ServerProtocol.IS_SAID_STOPMAKAO){
                    if(properties.getStopMakao() != null){
                        Logger.logConsole(TAG, "Sending StopMakao object");
                        send(ServerProtocol.PLAYER_SAID_STOPMAKAO);
                        send(properties.getStopMakao());
                        properties.setStopMakao(null);
                    }else send(ServerProtocol.PLAYER_NOT_SAID_STOPMAKAO);
                }else if((int) received == ServerProtocol.TURN_STARTED){
                    Logger.logConsole(TAG, "Turn started");
                    properties.setTurnStarted(true);
                    turnProcessing();
                }
            }
        }
    }

    private void updatePlayersInformation(){
        command = -1;
        updatedPlayers = new ArrayList<>();

        while(!(command == ServerProtocol.STOP_UPDATE)){
            handleInput();
        }
        properties.setAdditionalPlayers(updatedPlayers);

        if(properties.getPuttedCards().size() > 0){
            int indexOfLastCard = properties.getPuttedCards().size() - 1;
            properties.setCardOnTop(properties.getPuttedCards().get(indexOfLastCard));
        }
        properties.setUpdateGame(true);
    }

    private void handleInput(){
        Logger.logConsole(TAG, "Waiting for command");
        Object received = receive();
        if(received instanceof Player){
            receivedPlayer((Player) received);
        }else if(received instanceof Card && ((Card) received).getFunction().getFunctionID() == Function.ORDERED){
            receivedOrderedCard((Card) received);
        }else if(received instanceof Card){
            //getting normal card
            receivedCard((Card) received);
        }else if(received instanceof Integer){
            receivedCommand((Integer) received);
        }
    }

    private void receivedPlayer(Player player){
        if(player.getPlayerID() != properties.getLocalPlayer().getPlayerID()){
            Logger.logConsole(TAG, "Updating another player object");

            int numberOfCardsInHand = player.getCardsInHand().size();
            player.getCardsInHand().clear();
            for(int i = 0; i < numberOfCardsInHand; i++){
                player.getCardsInHand().add(new Card(0, 0, new Function(Function.NOTHING, -1)));
            }// TODO: 05.06.2016 check if working
            updatedPlayers.add(player);
        }else{
            properties.setLocalPlayer(player);
            Logger.logConsole(TAG, "Local player object from updating have: "
                    + player.getCardsInHand().size() + " cards in hand");
        }
    }

    private void receivedCard(Card card){
        Logger.logConsole(TAG, " ---- updated put card");
        properties.addPuttedCard(card);
    }

    private void receivedOrderedCard(Card card){
        Logger.logConsole(TAG, " ---- updated ordered card");
        properties.setOrderedCard(card);
    }

    private void receivedCommand(int received){
        command = received;
    }

    private void turnProcessing(){
        while(!properties.isTurnEnded()){
            Logger.logConsole(TAG, "waiting for turn end by player");
            try{
                Thread.sleep(2000);
            }catch(InterruptedException e){
                Logger.logError(e);
            }
        }

        Logger.logConsole(TAG, "Started sending cards");

        if(properties.getCardsToPut().size() > 0){
            List<Card> cardsToPut = properties.getCardsToPut();
            for(Card card : cardsToPut){
                Object received;
                if(isOrderCard(card)){
                    send(card);
                    received = receive();
                    if(received instanceof Integer && (int) received == ServerProtocol.GOT_ORDER_CARD){
                        send(properties.getOrderedCard());
                        received = receive();
                        if(received instanceof Integer && (int) received == ServerProtocol.CARD_NOTACCEPTED){
                            properties.addNotAcceptedCard(card);
                        }
                    }
                }else{
                    send(card);
                    received = receive();
                    if(received instanceof Integer && (int) received == ServerProtocol.CARD_NOTACCEPTED){
                        properties.addNotAcceptedCard(card);
                    }
                }
            }

            if(properties.getNotAcceptedCards().size() > 0){
                Logger.logConsole(TAG, "Cards rejected");
                properties.setTurnEnded(false);
                properties.setTurnStarted(true);
                properties.setCardsRejected(true);
                properties.setUpdateGame(true);
                turnProcessing();
            }else{
                properties.setCardsRejected(false);
                endingTurn();
            }
        }else if(properties.getCardsToPut().size() == 0){
            properties.setCardsRejected(false);
            endingTurn();
        }
    }

    private boolean isOrderCard(Card card){
        return card.getFunction().getFunctionID() == Function.ORDER_CARD ||
                card.getFunction().getFunctionID() == Function.CHANGE_COLOR;
    }

    private void endingTurn(){
        Logger.logConsole(TAG, "Ending turn");
        properties.setTurnEnded(false);
        if(properties.getLocalPlayer().isMakao()){
            Logger.logConsole(TAG, "Setting makao");
            send(ServerProtocol.PLAYER_SET_MAKAO);
        }
        send(ServerProtocol.TURN_ENDED);
        Object received = receive();
        if(received instanceof Player){
            Logger.logConsole(TAG, "Received player object updated after turn ending");
            properties.setLocalPlayer((Player) received);
            Logger.logConsole(TAG, "Received player object after ending turn have: "
                    + ((Player) received).getCardsInHand().size() + " cards in hand");
            if(((Player) received).getCardsInHand().size() == 0){
                Logger.logConsole(TAG, "Player ended turn");
                properties.setGameEnded(true);
            }
        }else{
            endingTurn();
        }
        received = receive();
        if(received instanceof Card){
            Logger.logConsole(TAG, "Received card object updated after turn ending");
            properties.setCardOnTop((Card) received);
        }else{
            endingTurn();
        }
        properties.setUpdateGame(true);
    }

    private void send(Object object){
        //http://stackoverflow.com/a/12341193/5509049
        conn.send(object);
    }

    private Object receive(){
        //http://stackoverflow.com/a/12341193/5509049
        return conn.receive();
    }
}


