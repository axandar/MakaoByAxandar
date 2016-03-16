package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 14.02.2016.
 */
public class SessionInfo {

    private boolean isGameExited = false;
    private boolean isGameStarted = false;
    private List<ClientConnectObject> clients = new ArrayList<>();
    private List<Player> players = new ArrayList<>();
    private int playersNotReady;
    private int justEndedTurnPlayerId = 0;//inicjalizacja jako pierwszy gracz w tabeli

    public SessionInfo() {

    }

    public boolean isGameExited() {
        return isGameExited;
    }

    public void setGameExited(boolean gameExited) {
        isGameExited = gameExited;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        isGameStarted = gameStarted;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public int decreasePlayersNotReady() {
        return playersNotReady--;
    }

    public void increasePlayersNotReady() {
        playersNotReady++;
    }

    public int getNumberOfPlayersNotReady() {
        return playersNotReady;
    }

    public List<ClientConnectObject> getClients(){
        return clients;
    }

    public int getJustEndedTurnPlayerId(){
        return justEndedTurnPlayerId;
    }

    public void setJustEndedTurnPlayerId(int justEndedTurnPlayerId){
        this.justEndedTurnPlayerId = justEndedTurnPlayerId;
    }
}
