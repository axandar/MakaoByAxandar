package com.axandar.makaoClient;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 12.03.2016.
 */
public class ClientProperties{

    private String nickname;
    private String ip;
    private int port;
    private boolean isClientRunning;
    private List<Player> aditionalPlayers = new ArrayList<>();
    private int estimatedPlayersNumber;
    private Player localPlayer;
    private Card cardOnTop;
    private List<Card> puttedCards = new ArrayList<>();

    public String getNickname(){
        return nickname;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
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

    public boolean isClientRunning(){
        return isClientRunning;
    }

    public void setClientRunning(boolean clientRunning){
        isClientRunning = clientRunning;
    }

    public List<Player> getAditionalPlayers(){
        return aditionalPlayers;
    }

    public void setAditionalPlayers(List<Player> aditionalPlayers){
        this.aditionalPlayers = aditionalPlayers;
    }

    public void addPlayer(Player player){
        aditionalPlayers.add(player);
    }

    public int getEstimatedPlayersNumber(){
        return estimatedPlayersNumber;
    }

    public void setEstimatedPlayersNumber(int estimatedPlayersNumber){
        this.estimatedPlayersNumber = estimatedPlayersNumber;
    }

    public Player getLocalPlayer(){
        return localPlayer;
    }

    public void setLocalPlayer(Player localPlayer){
        this.localPlayer = localPlayer;
    }

    public Card getCardOnTop(){
        return cardOnTop;
    }

    public void setCardOnTop(Card cardOnTop){
        this.cardOnTop = cardOnTop;
    }

    public List<Card> getPuttedCards(){
        return puttedCards;
    }

    public void setPuttedCards(List<Card> puttedCards){
        this.puttedCards = puttedCards;
    }

    public void addPuttedCard(Card card){
        puttedCards.add(card);
    }
}
