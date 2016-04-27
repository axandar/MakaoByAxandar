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

    private final String TAG = "Client backend";

    private String ip;
    private int port;
    private String nickname;
    private ClientProperties properties;
    private Connection conn;

    public Client(ClientProperties _properties) {
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
            /**ObjectInputStream obj = new ObjectInputStream(connectionToServer.getInputStream());
            conn = new Connection(new ObjectInputStream(connectionToServer.getInputStream()),
                    new ObjectOutputStream(connectionToServer.getOutputStream()));**/
        //WEIRD ERROR, ENDLESS LOOP ON INITIALIZE
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
            properties.setEstimatedPlayersNumber((int)received);
        }

        received = receive();
        if(received instanceof Integer && (int)received == ServerProtocol.GAME_STARTED){
            received = receive();
            if(received instanceof Player){
                properties.setLocalPlayer((Player)received);
            }

            int i = 1;
            while(i < properties.getEstimatedPlayersNumber()){
                received = receive();
                if(received instanceof Player){
                    i++;
                    properties.addPlayer((Player)received);
                    Logger.logConsole(TAG, "Added player: " + ((Player) received).getPlayerName());
                }
            }

            received = receive();
            if(received instanceof Card){
                Logger.logConsole(TAG, "is card on top null: " + ((Card)received == null));
                properties.setCardOnTop((Card)received);
            }
            Logger.logConsole(TAG, "First update need");
            properties.setUpdateGame(true);
            handleCommands();
        }

    }

    private void handleCommands(){
        while(properties.isClientRunning()){
            Object received = receive();
            if(received instanceof Integer){
                if((int)received == ServerProtocol.START_UPDATE){
                    Logger.logConsole(TAG, "Start update players");
                    updatePlayersInformation();
                }else if((int)received == ServerProtocol.IS_SAID_STOPMAKAO){
                    Logger.logConsole(TAG, "is stop makao said there?");
                    if(properties.isSaidMakao()){
                        send(ServerProtocol.PLAYER_SAID_STOPMAKAO);
                        send(properties.getStopMakao());
                        properties.setSaidMakao(false);
                    }else send(ServerProtocol.PLAYER_NOT_SAID_STOPMAKAO);
                }else if((int)received == ServerProtocol.TURN_STARTED){
                    Logger.logConsole(TAG, "Turn started");
                    turnProcessing();

                }
            }
        }
    }

    private void updatePlayersInformation(){
        List<Player> updatedPlayers = new ArrayList<>();
        Object received = receive();
        while(!(received instanceof Integer && (int)received == ServerProtocol.STOP_UPDATE)){
            if(received instanceof Player){
                Logger.logConsole(TAG, " ---- updated player");
                if(((Player) received).getPlayerID() != properties.getLocalPlayer().getPlayerID()){
                    updatedPlayers.add((Player)received);
                }else properties.setLocalPlayer((Player)received);
            }else if(received instanceof Card){
                Logger.logConsole(TAG, " ---- updated card");
                properties.addPuttedCard((Card)received);
            }
        }
        properties.setAditionalPlayers(updatedPlayers);
        int indexOfLastCard = properties.getPuttedCards().size()-1;
        properties.setCardOnTop(properties.getPuttedCards().get(indexOfLastCard));
        properties.setUpdateGame(true);
    }

    private void turnProcessing(){
        while(!properties.isTurnEnded()){
            try{
                Thread.sleep(2000);
            }catch(InterruptedException e){
                Logger.logError(e);
            }
        }
        Logger.logConsole(TAG, "Started sending cards");
        if(properties.getCardsToPut().size() > 0){
            List<Card> cardsToPut = properties.getCardsToPut();
            boolean isCardsEquals = true;
            for(int i = 1; i < cardsToPut.size(); i++){
                if(!cardsToPut.get(i-1).getFunction().equals(cardsToPut.get(i).getFunction())){
                    isCardsEquals = false;
                }
            }

            if(isCardsEquals){
                Logger.logConsole(TAG, "Cards equal");
                for(Card card:cardsToPut){
                    Object received;
                    if(isOrderCard(card)){
                        send(card);
                        received = receive();
                        if(received instanceof Integer && (int)received == ServerProtocol.GOT_ORDER_CARD){
                            send(properties.getOrderedCard());
                            received = receive();
                            if(received instanceof Integer && (int)received == ServerProtocol.CARD_NOTACCEPTED){
                                properties.addNotAcceptedCard(card);
                            }
                        }
                    }else{
                        send(card);
                        received = receive();
                        if(received instanceof Integer && (int)received == ServerProtocol.CARD_NOTACCEPTED){
                            properties.addNotAcceptedCard(card);
                        }
                    }
                }
            }else{
                Logger.logConsole(TAG, "Cards not equal");
                properties.setTurnEnded(false);
                properties.setCardsRejected(true);
                properties.setUpdateGame(true);
                turnProcessing();
            }
        }


        if(properties.getNotAcceptedCards().size() > 0){
            Logger.logConsole(TAG, "Cards rejected");
            properties.setTurnEnded(false);
            properties.setCardsRejected(true);
            properties.setUpdateGame(true);
            turnProcessing();
        }else{
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
        if(properties.getLocalPlayer().isMakao()){
            Logger.logConsole(TAG, "Setting makao");
            send(ServerProtocol.PLAYER_SET_MAKAO);
        }
        send(ServerProtocol.TURN_ENDED);
        Object received = receive();
        if(received instanceof Player){
            Logger.logConsole(TAG, "Received player object updated after turn ending");
            properties.setLocalPlayer((Player) received);
            if(((Player)received).getCardsInHand().size() == 0){
                Logger.logConsole(TAG, "Player ended turn");
                properties.setGameEnded(true);
            }
        }
        properties.setUpdateGame(true);
    }

    private void send(Object object){
        conn.send(object);
    }

    private Object receive(){
        return conn.receive();
    }
}


