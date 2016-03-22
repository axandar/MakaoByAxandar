package com.axandar.makaoClient;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
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

    // TODO: 22.03.2016 make handle error in "instanceof" statements(some kind of error handling)

    private String ip;
    private int port;
    private String nickname;
    private ClientProperties properties;

    private Socket connectionToServer = null;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    public Client(ClientProperties _properties) {
        properties = _properties;
        ip = properties.getIp();
        port = properties.getPort();
        nickname = properties.getNickName();

    }

    private boolean startConnection(){
        try{
            connectionToServer = new Socket(ip, port);
            fromServer = new ObjectInputStream(connectionToServer.getInputStream());
            toServer = new ObjectOutputStream(connectionToServer.getOutputStream());
            properties.clientStarted();
            return true;
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void run(){
        if(startConnection()) try{
            setNickname();
            handleCommand();
        }catch(IOException | ClassNotFoundException | InterruptedException e){
            logError(e);
        }
    }

    private void setNickname() throws IOException{
        toServer.writeObject(nickname);
    }

    private void handleCommand() throws IOException, ClassNotFoundException, InterruptedException{
        Object objectFromServer = fromServer.readObject();

        if(objectFromServer instanceof Integer){
            int receivedCommand = (int) objectFromServer;
            if(receivedCommand == ServerProtocol.ACCEPTED_NICK){
                handleCommand();
            }else if(receivedCommand == ServerProtocol.GAME_STARTED){
                getPlayerObject();
                getRestPlayersInfo();
                startGame();
            }
        }
    }

    private void getPlayerObject() throws IOException, ClassNotFoundException{
        Object objectFromServer = fromServer.readObject();
        if(objectFromServer instanceof Player){
            properties.setPlayer((Player) objectFromServer);
            properties.updateGame();
        }
    }

    private void getRestPlayersInfo() throws IOException, ClassNotFoundException{
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Integer){
            if((int) receivedObject == ServerProtocol.START_UPDATE_PLAYERS){
                boolean isUpdatingPlayers = true;
                while(isUpdatingPlayers){
                    receivedObject = fromServer.readObject();
                    if(receivedObject instanceof Player){
                        properties.addPlayerToList((Player) receivedObject);
                    }else if(receivedObject instanceof Integer){
                        if((int) receivedObject == ServerProtocol.END_UPDATE_PLAYERS){
                            isUpdatingPlayers = false;
                        }
                    }
                }
                properties.updateGame();
            }
        }
    }

    private void startGame() throws IOException, ClassNotFoundException, InterruptedException{
        int receivedCommand = -1;
        while(properties.isClientRunning()){
            Object receivedObject = fromServer.readObject();
            if(receivedObject instanceof Integer){
                receivedCommand = (Integer) receivedObject;
            }

            if(receivedCommand == ServerProtocol.START_UPDATE_PLAYERS){
                updatePlayers();
                setCardOnTop();//When other player end turn need to update cardOnTop
                //each statement is updating all players information and ending turn of one player
            }else if(receivedCommand == ServerProtocol.TURN_STARTED){
                while(!properties.isTurnEnded()){
                    Card cardToSend = properties.getCardToPut();
                    if(cardToSend != null){
                        if(cardToSend.getFunction().getFunctionID() == Function.ORDER_CARD
                                || cardToSend.getFunction().getFunctionID() == Function.CHANGE_COLOR){
                            // TODO: 22.03.2016 Send order card to server
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
                                            properties.setCardToPut(null);
                                            //ON CLIENT "if cardToPut is null && cardAccepted = true => then can delete card from hand"
                                            properties.setCardAccepted(true);
                                        }else if(receivedCommand == ServerProtocol.CARD_NOTACCEPTED){
                                            properties.setCardAccepted(false);
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
                                    properties.setCardToPut(null);
                                    properties.setCardAccepted(true);
                                }else if(receivedCommand == ServerProtocol.CARD_NOTACCEPTED){
                                    properties.setCardAccepted(false);
                                }
                            }
                        }
                    }
                }

                if(properties.isMakaoSet()){
                    setMakao();
                }

                endTurnOnServer();

                receivedObject = fromServer.readObject();
                if(receivedObject instanceof Player){
                    properties.setPlayer((Player) receivedObject);
                }

            }
        }
    }

    private void updatePlayers() throws IOException, ClassNotFoundException{
        int receivedCommand = -1;
        while(receivedCommand != ServerProtocol.END_UPDATE_PLAYERS){
            Object receivedObject = fromServer.readObject();

            if(receivedObject instanceof Integer){
                receivedCommand = (int) receivedObject;
            }else if(receivedObject instanceof Player){
                properties.setPlayerToUpdate((Player) receivedObject);
                properties.updateGame();
            }
        }
    }

    private void setCardOnTop() throws IOException, ClassNotFoundException{
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Card){
            properties.setCardOnTop((Card) receivedObject);
            properties.updateGame();
        }
    }

    private void setMakao() throws IOException, ClassNotFoundException{
        // TODO: 22.03.2016 set throw error when tried send command too many times
        toServer.writeObject(ServerProtocol.PLAYER_SET_MAKAO);
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Integer){
            int receivedCommand = (int) receivedObject;
            if(receivedCommand != ServerProtocol.GOT_CMD){
                setMakao();
            }
        }
    }

    private void endTurnOnServer() throws IOException, ClassNotFoundException{
        // TODO: 22.03.2016 set throw error when tried send command too many times
        toServer.writeObject(ServerProtocol.TURN_ENDED);
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Integer){
            int receivedCommand = (int) receivedObject;
            if(receivedCommand != ServerProtocol.GOT_CMD){
                endTurnOnServer();
            }
        }
    }
}


