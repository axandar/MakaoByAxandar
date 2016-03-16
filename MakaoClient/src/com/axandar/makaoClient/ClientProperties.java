package com.axandar.makaoClient;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Player;

/**
 * Created by Axandar on 12.03.2016.
 */
public class ClientProperties{

    private volatile String nickName;
    private volatile String ip;
    private volatile int port;

    private volatile boolean isGameUpdate;
    private volatile boolean isClientRunning;
    private volatile int command;
    private volatile Card card;
    private volatile Card requestedCard;
    private volatile Player player;
    private volatile Player playerToUpdate;

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

    public int getCommand(){
        return command;
    }

    public void setCommand(int command){
        this.command = command;
    }

    public Card getCard(){
        return card;
    }

    public void setCard(Card card){
        this.card = card;
    }

    public Card getRequestedCard(){
        return requestedCard;
    }

    public void setRequestedCard(Card requestedCard){
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
}
