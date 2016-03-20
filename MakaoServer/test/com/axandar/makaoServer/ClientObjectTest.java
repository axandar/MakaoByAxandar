package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.ServerProtocol;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Axandar on 20.03.2016.
 */
public class ClientObjectTest{

    @Test
    public void testTurn() throws IOException, ClassNotFoundException{

        TableServer table = new TableServer();
        GameSession gs = new GameSession(1, 1, Main.initializeFunctions(), 5000){
            @Override
            public TableServer setTableServer(){
                return table;
            }
        };

        Player player;

        Thread thread = new Thread(gs);
        thread.start();
        Socket socket = new Socket("192.168.0.100", 5000);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

        outputStream.writeObject("nickname");
        Object receivedFromServer = inputStream.readObject();
        assertEquals("Received command to nick acception", receivedFromServer instanceof Integer, true);
        int receivedComand = (int) receivedFromServer;
        assertEquals("Accepted nick", receivedComand, ServerProtocol.ACCEPTED_NICK);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to game starting", receivedFromServer instanceof Integer, true);
        receivedComand = (int) receivedFromServer;
        assertEquals("Game started", receivedComand, ServerProtocol.GAME_STARTED);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received player", receivedFromServer instanceof Player, true);
        player = (Player) receivedFromServer;

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to start updating players",
                receivedFromServer instanceof Integer, true);

        receivedComand = (int) receivedFromServer;
        assertEquals("Start updating players", receivedComand, ServerProtocol.START_UPDATE_PLAYERS);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received player", receivedFromServer instanceof Player, true);
        player = (Player) receivedFromServer;

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to end updating players",
                receivedFromServer instanceof Integer, true);

        receivedComand = (int) receivedFromServer;
        assertEquals("End updating players", receivedComand, ServerProtocol.END_UPDATE_PLAYERS);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to Turn starting", receivedFromServer instanceof Integer, true);
        receivedComand = (int) receivedFromServer;
        assertEquals("Turn started", receivedComand, ServerProtocol.TURN_STARTED);

        Card cardOnTopOnTable = table.getCardOnTop();
        outputStream.writeObject(cardOnTopOnTable);
        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to got card", receivedFromServer instanceof Integer, true);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server got card", receivedComand, ServerProtocol.GOT_CARD);
        // TODO: 20.03.2016 20% chance to not receive card

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to accept card", receivedFromServer instanceof Integer, true);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server accepted card", receivedComand, ServerProtocol.CARD_ACCEPTED);

        int lastCardColorID = cardOnTopOnTable.getIdColor();
        if(lastCardColorID == 3){
            lastCardColorID = 0;
        }else lastCardColorID++;

        int lastCardTypeID = cardOnTopOnTable.getIdType();
        if(lastCardTypeID == 13){
            lastCardTypeID = 1;
        }else lastCardTypeID++;
        Card errorCard = new Card(lastCardColorID, lastCardTypeID, new Function(6, 0));

        outputStream.writeObject(errorCard);
        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to got card", receivedFromServer instanceof Integer, true);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server got card", receivedComand, ServerProtocol.GOT_CARD);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to not accept card", receivedFromServer instanceof Integer, true);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server not accepted card", receivedComand, ServerProtocol.CARD_NOTACCEPTED);

        outputStream.writeObject(ServerProtocol.TURN_ENDED);
        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to Command", receivedFromServer instanceof Integer, true);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server got Command", receivedComand, ServerProtocol.GOT_CMD);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received Player object", receivedFromServer instanceof Player, true);
        assertEquals("Server send Player",
                receivedFromServer instanceof Player, true);

        gs.closeSocket();
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
