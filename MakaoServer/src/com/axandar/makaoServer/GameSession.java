package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Axandar on 11.02.2016.
 */
public class GameSession implements Runnable{

    private List<Thread> clientsThreads = new ArrayList<>();
    private List<ClientConnectObject> clients = new ArrayList<>();

    private static List<List<Function>> functions = new ArrayList<>();
    private static int numberOfDecks;
    private static int numberOfPlayers;
    private static int port;

    private ServerSocket sSocket;

    private volatile SessionInfo sessionInfo;
    private volatile TableServer table = setTableServer();

    public GameSession(int _numberOfPlayers, int _numberOfDecks, List<List<Function>> _functions, int _port) {
        numberOfPlayers = _numberOfPlayers;
        numberOfDecks = _numberOfDecks;
        functions = _functions;
        port = _port;
    }

    public void run(){
        sessionInfo = new SessionInfo();

        try {
            sSocket = new ServerSocket(port);
            Logger.logConsole("Starting server", "Server started at: " + new Date() + " at port: " + port);
            Logger.logConsole("Starting server", "Server started at ip: " + sSocket.getInetAddress().toString());

            settingUpPlayers(sSocket);
            waitForPlayersGetReady();
            table.initializeGame();

        } catch(IOException | InterruptedException e) {
            Logger.logError(e);
        }
    }

    public void settingUpPlayers(ServerSocket serverSocket) throws IOException{
        int actualId = 0;
        while(actualId < numberOfPlayers) {
            sessionInfo.increasePlayersNotReady();
            Socket socket = serverSocket.accept();

            ClientConnectObject clientConnectionObject = new ClientConnectObject(socket, actualId, sessionInfo);
            clients.add(clientConnectionObject);
            actualId++;

            Thread clientThread = new Thread(clientConnectionObject);
            clientsThreads.add(clientThread);
            clientThread.start();
        }
    }

    private void waitForPlayersGetReady() throws InterruptedException{
        while(sessionInfo.getPlayersNotReady() > 0){
            Thread.sleep(1000);
        }
    }

    public void closeSocket(){
        try{
            sSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public TableServer setTableServer(){
        return new TableServer(sessionInfo, numberOfPlayers, numberOfDecks, functions);
    }
}