package com.axandar.makaoClient;

import com.axandar.makaoCore.logic.Card;
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

        /**Socket socketConnection = null;
        try {
            socketConnection = new Socket("127.0.0.1", 5000);

            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socketConnection.getOutputStream());
            ObjectInputStream clientInputStream = new ObjectInputStream(socketConnection.getInputStream());

            clientOutputStream.writeObject("Axandar");

            Object receivedMessage = clientInputStream.readObject();

            if(receivedMessage instanceof Integer){
                System.out.println((Integer) receivedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }**/

        /**Socket socketConnection = null;
        try {
            socketConnection = new Socket("127.0.0.1", 5000);

            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socketConnection.getOutputStream());
            //ObjectInputStream clientInputStream = new ObjectInputStream(socketConnection.getInputStream());

            clientOutputStream.writeObject(1);
            Thread.sleep(10000);
            clientOutputStream.writeObject(2);

        } catch (IOException e) {
            e.printStackTrace();
        }**/

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
            Object objectFromServer = fromServer.readObject();

            if(objectFromServer instanceof Integer){
                int receivedFromServer = (Integer) objectFromServer;
                if(receivedFromServer == ServerProtocol.GAME_STARTED){
                    getPlayerObject();
                    getRestPlayersInfo();
                    startGame();
                }
            }
        }catch(IOException | ClassNotFoundException | InterruptedException e){
            logError(e);
        }
    }

    private void setNickname() throws IOException{
        toServer.writeObject(nickname);
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
                while((int) receivedObject != ServerProtocol.END_UPDATE_PLAYERS){
                    receivedObject = fromServer.readObject();
                    if(receivedObject instanceof Player){
                        properties.addPlayerToList((Player) receivedObject);
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
                receivedCommand = updatePlayers();
                setCardOnTop();//When other player end turn need to update cardOnTop
            }

            receivedObject = fromServer.readObject();
            if((receivedObject instanceof Integer) &&
                    (Integer) receivedObject == ServerProtocol.TURN_STARTED ){
                //poczatek tury postronie klienta
                while(properties.getCommand() != ServerProtocol.TURN_ENDED){
                    if(properties.getCommand() == ServerProtocol.SEND_CARD_NORMAL){
                        toServer.writeObject(properties.getCard());
                        receivedObject = fromServer.readObject();
                        if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.GOT_CARD){
                            properties.setCommand(ServerProtocol.GOT_CARD);
                            receivedObject = fromServer.readObject();
                            if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.CARD_ACCEPTED){
                                properties.setCommand(ServerProtocol.CARD_ACCEPTED);
                            }else if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.CARD_NOTACCEPTED){
                                properties.setCommand(ServerProtocol.CARD_NOTACCEPTED);
                            }
                        }else if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.GOT_ORDER_CARD){
                            properties.setCommand(ServerProtocol.GOT_ORDER_CARD);
                            toServer.writeObject(properties.getRequestedCard());
                            receivedObject = fromServer.readObject();
                            if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.CARD_ACCEPTED){
                                properties.setCommand(ServerProtocol.CARD_ACCEPTED);
                            }else if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.CARD_NOTACCEPTED){
                                properties.setCommand(ServerProtocol.CARD_NOTACCEPTED);
                            }
                        }
                    }
                    // TODO: 13.03.2016 wyslanie informacji gdy gracz ma makao

                    receivedObject = fromServer.readObject();
                    if(receivedObject instanceof Player){
                        properties.setPlayer((Player) receivedObject);
                    }

                    if(properties.getPlayer().isMakao()){
                        toServer.writeObject(ServerProtocol.PLAYER_SET_MAKAO);
                    }
                    Thread.sleep(1000);
                }

                toServer.writeObject(ServerProtocol.TURN_ENDED);
                properties.updateGame();
            }
        }
    }

    private int updatePlayers() throws IOException, ClassNotFoundException{
        Object receivedObject;
        int receivedCommand = -1;
        while(receivedCommand != ServerProtocol.END_UPDATE_PLAYERS){
            receivedObject = fromServer.readObject();

            if(receivedObject instanceof Integer){
                receivedCommand = (Integer) receivedObject;
            }else if(receivedObject instanceof Player){
                properties.setPlayerToUpdate((Player) receivedObject);
                properties.updateGame();
            }
        }
        return receivedCommand;
    }

    private void setCardOnTop() throws IOException, ClassNotFoundException{
        Object receivedObject = fromServer.readObject();
        if(receivedObject instanceof Card){
            properties.setCardOnTop((Card) receivedObject);
            properties.updateGame();
        }
    }

    /**{
        private void startGame() throws IOException, ClassNotFoundException, InterruptedException{
        while(properties.isClientRunning()){
            Object receivedObjectFromServer = fromServer.readObject();
            int receivedCommand = -1;
            if(receivedObjectFromServer instanceof Integer){
                receivedCommand = (Integer) receivedObjectFromServer;
            }

            if(receivedCommand == ServerProtocol.START_UPDATE_PLAYERS){
                while(receivedCommand != ServerProtocol.END_UPDATE_PLAYERS){
                    receivedObjectFromServer = fromServer.readObject();

                    if(receivedObjectFromServer instanceof Integer){
                        receivedCommand = (Integer) receivedObjectFromServer;
                    }else if(receivedObjectFromServer instanceof Player){
                        properties.setPlayerToUpdate((Player) receivedObjectFromServer);
                        properties.updateGame();
                    }
                }

                receivedObjectFromServer = fromServer.readObject();
                if(receivedObjectFromServer instanceof Card){
                    properties.setCardOnTop((Card) receivedObjectFromServer);
                }
            }

            receivedObjectFromServer = fromServer.readObject();
            if((receivedObjectFromServer instanceof Integer) &&
                    (Integer) receivedObjectFromServer == ServerProtocol.TURN_STARTED ){
                //poczatek tury postronie klienta
                while(properties.getCommand() != ServerProtocol.TURN_ENDED){
                    if(properties.getCommand() == ServerProtocol.SEND_CARD_NORMAL){
                        toServer.writeObject(properties.getCard());
                        Object receivedObject = fromServer.readObject();
                        if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.GOT_CARD){
                            properties.setCommand(ServerProtocol.GOT_CARD);
                            receivedObject = fromServer.readObject();
                            if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.CARD_ACCEPTED){
                                properties.setCommand(ServerProtocol.CARD_ACCEPTED);
                            }else if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.CARD_NOTACCEPTED){
                                properties.setCommand(ServerProtocol.CARD_NOTACCEPTED);
                            }
                        }else if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.GOT_ORDER_CARD){
                            properties.setCommand(ServerProtocol.GOT_ORDER_CARD);
                            toServer.writeObject(properties.getRequestedCard());
                            receivedObject = fromServer.readObject();
                            if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.CARD_ACCEPTED){
                                properties.setCommand(ServerProtocol.CARD_ACCEPTED);
                            }else if(receivedObject instanceof Integer && (Integer) receivedObject == ServerProtocol.CARD_NOTACCEPTED){
                                properties.setCommand(ServerProtocol.CARD_NOTACCEPTED);
                            }
                        }
                    }
                    // TODO: 13.03.2016 wyslanie informacji gdy gracz ma makao

                    receivedObjectFromServer = fromServer.readObject();
                    if(receivedObjectFromServer instanceof Player){
                        properties.setPlayer((Player) receivedObjectFromServer);
                    }

                    if(properties.getPlayer().isMakao()){
                        toServer.writeObject(ServerProtocol.PLAYER_SET_MAKAO);
                    }
                    Thread.sleep(1000);
                }

                toServer.writeObject(ServerProtocol.TURN_ENDED);
                properties.updateGame();
            }
        }
    }
    }**/
}


