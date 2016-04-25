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

import static com.axandar.makaoCore.utils.Logger.logError;

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
        nickname = properties.getNickName();

    }

    @Override
    public void run(){
        Logger.logConsole(TAG, "Started client backend");
        properties.clientStarted();
        startConnection();
        setNickname();
        handleCommand();
    }

    private boolean startConnection(){
        try{
            Socket connectionToServer = new Socket(ip, port);
            conn = new Connection(new ObjectInputStream(connectionToServer.getInputStream()),
                    new ObjectOutputStream(connectionToServer.getOutputStream()));
            properties.clientStarted();
            return true;
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    private void setNickname(){
        toServer.writeObject(nickname);
    }

    private void handleCommand(){
        Object objectFromServer = fromServer.readObject();
        if(objectFromServer instanceof Integer){
            Logger.logConsole(TAG, "Received command as:" + (int) objectFromServer);
            int receivedCommand = (int) objectFromServer;
            if(receivedCommand == ServerProtocol.ACCEPTED_NICK){
                handleCommand();
            }else if(receivedCommand == ServerProtocol.GAME_STARTED){
                Logger.logConsole(TAG, "Game started at backend");
                getPlayerObject();
                getRestPlayersInfo();
                startGame();
            }else{
                handleCommand();
            }
        }
    }

    private void getPlayerObject(){
        Object objectFromServer = fromServer.readObject();
        if(objectFromServer instanceof Player){
            properties.setPlayer((Player) objectFromServer);
            Logger.logConsole(TAG, "Received new player object");
            properties.updateGame();
        }
    }

    private void getRestPlayersInfo(){
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Integer){
            if((int) receivedObject == ServerProtocol.START_UPDATE){
                boolean isUpdatingPlayers = true;
                while(isUpdatingPlayers){
                    receivedObject = fromServer.readObject();
                    if(receivedObject instanceof Player){
                        properties.addPlayerToList((Player) receivedObject);
                    }else if(receivedObject instanceof Integer){
                        if((int) receivedObject == ServerProtocol.STOP_UPDATE){
                            isUpdatingPlayers = false;
                        }
                    }
                }
                properties.updateGame();
            }
        }
    }

    private void startGame(){
        int receivedCommand = -1;
        while(properties.isClientRunning()){
            Object receivedObject = fromServer.readObject();
            if(receivedObject instanceof Integer){
                receivedCommand = (Integer) receivedObject;
            }

            if(receivedCommand == ServerProtocol.START_UPDATE){
                setCardOnTop();//When other player end turn need to update cardOnTop
                updatePlayers();
                //each statement is updating all players information and ending turn of one player
            }else if(receivedCommand == ServerProtocol.TURN_STARTED){
                properties.startTurn();
                setCardOnTop();
                while(!properties.isTurnEnded()){
                    Card cardToSend = properties.getCardToPut();
                    if(cardToSend != null){
                        if(cardToSend.getFunction().getFunctionID() == Function.ORDER_CARD
                                || cardToSend.getFunction().getFunctionID() == Function.CHANGE_COLOR){
                            toServer.writeObject(cardToSend);
                            receivedObject = fromServer.readObject();
                            if(receivedObject instanceof Integer){
                                receivedCommand = (int) receivedObject;
                                if(receivedCommand == ServerProtocol.GOT_ORDER_CARD){
                                    toServer.writeObject(properties.getRequestedCard());
                                    receivedObject = fromServer.readObject();
                                    if(receivedObject instanceof Integer){
                                        receivedCommand = (int) receivedObject;
                                        if(receivedCommand == ServerProtocol.CARD_ACCEPTED){
                                            Logger.logConsole(TAG, "Order card accepted");
                                            properties.setCardToPut(null);
                                            //ON CLIENT "if cardToPut is null && cardAccepted = true => then can delete card from hand"
                                            properties.setCardAccepted(true);
                                            Player player = properties.getPlayer();
                                            player.removeCardFromHand(cardToSend);
                                            properties.setPlayer(player);
                                            properties.updateGame();
                                        }else if(receivedCommand == ServerProtocol.CARD_NOTACCEPTED){
                                            Logger.logConsole(TAG, "Order card not accepted");
                                            properties.setCardToPut(null);
                                            properties.setCardAccepted(false);
                                            properties.updateGame();
                                        }
                                    }
                                }
                            }
                        }else{
                            toServer.writeObject(cardToSend);
                            receivedObject = fromServer.readObject();
                            if(receivedObject instanceof Integer){
                                receivedCommand = (int) receivedObject;
                                if(receivedCommand == ServerProtocol.CARD_ACCEPTED){
                                    Logger.logConsole(TAG, "Card accepted");
                                    properties.setCardToPut(null);
                                    properties.setCardAccepted(true);
                                    Player player = properties.getPlayer();
                                    player.removeCardFromHand(cardToSend);
                                    properties.setPlayer(player);
                                    properties.updateGame();
                                }else if(receivedCommand == ServerProtocol.CARD_NOTACCEPTED){
                                    Logger.logConsole(TAG, "Card not accepted");
                                    properties.setCardToPut(null);
                                    properties.setCardAccepted(false);
                                    properties.updateGame();
                                }
                            }
                        }
                    }
                    Thread.sleep(2000);
                }

                if(properties.isMakaoSet()){
                    setMakao();
                }

                endTurnOnServer();

                receivedObject = fromServer.readObject();
                if(receivedObject instanceof Player){
                    properties.setPlayer((Player) receivedObject);
                    properties.updateGame();
                }

            }
        }
    }

    private void updatePlayers(){
        int receivedCommand = -1;
        while(receivedCommand != ServerProtocol.STOP_UPDATE){
            Object receivedObject = fromServer.readObject();

            if(receivedObject instanceof Integer){
                receivedCommand = (int) receivedObject;
            }else if(receivedObject instanceof Player){
                properties.setPlayerToUpdate((Player) receivedObject);
                properties.updateGame();
            }
        }
    }

    private void setCardOnTop(){
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Card){
            properties.setCardOnTop((Card) receivedObject);
            properties.updateGame();
        }
    }

    private void setMakao(){
        toServer.writeObject(ServerProtocol.PLAYER_SET_MAKAO);
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Integer){
            int receivedCommand = (int) receivedObject;
            if(receivedCommand != ServerProtocol.GOT_CMD){
                setMakao();
            }else{
                properties.updateGame();
            }

        }
    }

    private void endTurnOnServer(){
        toServer.writeObject(ServerProtocol.TURN_ENDED);
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Integer){
            int receivedCommand = (int) receivedObject;
            if(receivedCommand != ServerProtocol.GOT_CMD){
                endTurnOnServer();
            }
        }
    }

    private void send(Object object){
        conn.send(object);
    }

    private Object receive(){
        return conn.receive();
    }
}


