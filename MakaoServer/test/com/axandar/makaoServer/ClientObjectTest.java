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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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
        assertEquals("Accepted nick", ServerProtocol.ACCEPTED_NICK, receivedComand);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to game starting", receivedFromServer instanceof Integer, true);
        receivedComand = (int) receivedFromServer;
        assertEquals("Game started", receivedComand, ServerProtocol.GAME_STARTED);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received player", true, receivedFromServer instanceof Player);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to start updating players",
                receivedFromServer instanceof Integer, true);

        receivedComand = (int) receivedFromServer;
        assertEquals("Start updating players", ServerProtocol.START_UPDATE_PLAYERS, receivedComand);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received player", true, receivedFromServer instanceof Player);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to end updating players", true,
                receivedFromServer instanceof Integer);

        receivedComand = (int) receivedFromServer;
        assertEquals("End updating players", ServerProtocol.END_UPDATE_PLAYERS, receivedComand);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to Turn starting", true, receivedFromServer instanceof Integer);
        receivedComand = (int) receivedFromServer;
        assertEquals("Turn started", ServerProtocol.TURN_STARTED, receivedComand);

        Card cardOnTopOnTable = table.getCardOnTop();
        outputStream.writeObject(cardOnTopOnTable);
        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to got card", true, receivedFromServer instanceof Integer);
        receivedComand = (int) receivedFromServer;
        assertThat("Server got card", receivedComand,
                anyOf(is(ServerProtocol.GOT_ORDER_CARD), is(ServerProtocol.GOT_CARD)));

        if(cardOnTopOnTable.getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                cardOnTopOnTable.getFunction().getFunctionID() == Function.ORDER_CARD){
            outputStream.writeObject(new Card(1, 1, new Function(6, 1)));
        }

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to accept card", true, receivedFromServer instanceof Integer);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server accepted card", ServerProtocol.CARD_ACCEPTED, receivedComand);

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
        assertEquals("Received command to got card", true, receivedFromServer instanceof Integer);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server got card", ServerProtocol.GOT_CARD, receivedComand);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to not accept card", true, receivedFromServer instanceof Integer);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server not accepted card", ServerProtocol.CARD_NOTACCEPTED, receivedComand);

        outputStream.writeObject(ServerProtocol.TURN_ENDED);
        receivedFromServer = inputStream.readObject();
        assertEquals("Received command to Command", true, receivedFromServer instanceof Integer);
        receivedComand = (int) receivedFromServer;
        assertEquals("Server got Command", ServerProtocol.GOT_CMD, receivedComand);

        receivedFromServer = inputStream.readObject();
        assertEquals("Received Player object", true, receivedFromServer instanceof Player);
        assertEquals("Server send Player", true,
                receivedFromServer instanceof Player);

        gs.closeSocket();
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
