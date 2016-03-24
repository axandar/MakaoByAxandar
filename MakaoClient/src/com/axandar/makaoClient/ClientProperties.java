package com.axandar.makaoClient;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 12.03.2016.
 */
public class ClientProperties{

    private volatile String nickName;
    private volatile String ip;
    private volatile int port;

    private volatile boolean isGameUpdate;
    private volatile boolean isClientRunning;
    private volatile Card cardOnTop;
    private volatile Card cardToPut;
    private volatile Card requestedCard;
    private volatile Player player;
    private volatile Player playerToUpdate;
    private volatile List<Player> players = new ArrayList<>();

    private volatile boolean isTurnEnded;
    private volatile boolean isCardAccepted;
    private volatile boolean isMakaoSet;

    public String getNickName(){
        return nickName;
    }

    public void setNickName(String nickName){
        this.nickName = nickName;
    }

    public String getIp(){
        return ip;
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public int getPort(){
        return port;
    }

    public void setPort(int port){
        this.port = port;
    }

    public boolean isGameUpdate(){
        return isGameUpdate;
    }

    public void updateGame(){
        isGameUpdate = true;
    }

    public void updatedGame(){
        isGameUpdate = false;
    }

    public boolean isClientRunning(){
        return isClientRunning;
    }

    public void clientStarted(){
        isClientRunning = true;
    }

    public void stopClient(){
        isClientRunning = false;
    }

    public Card getCardToPut(){
        return cardToPut;
    }

    public void setCardToPut(Card cardToPut){
        this.cardToPut = cardToPut;
    }

    public Card getRequestedCard(){
        return requestedCard;
    }

    public void setOrderedCard(Card requestedCard){
        this.requestedCard = requestedCard;
    }

    public Player getPlayer(){
        return player;
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public Player getPlayerToUpdate(){
        return playerToUpdate;
    }

    public void setPlayerToUpdate(Player playerToUpdate){
        this.playerToUpdate = playerToUpdate;
    }

    public Card getCardOnTop(){
        return cardOnTop;
    }

    public void setCardOnTop(Card cardOnTop){
        this.cardOnTop = cardOnTop;
    }

    public List<Player> getPlayers(){
        return players;
    }
    
    public void addPlayerToList(Player player){
        players.add(player.getPlayerID(), player);
    }

    public void endTurn(){
        isTurnEnded = true;
    }

    public void startTurn(){
        isTurnEnded = false;
    }

    public boolean isTurnEnded(){
        return isTurnEnded;
    }

    public boolean isCardAccepted(){
        return isCardAccepted;
    }

    public void setCardAccepted(boolean cardAccepted){
        isCardAccepted = cardAccepted;
    }

    public boolean isMakaoSet(){
        return isMakaoSet;
    }

    public void setMakaoSet(boolean makaoSet){
        isMakaoSet = makaoSet;
    }
}
