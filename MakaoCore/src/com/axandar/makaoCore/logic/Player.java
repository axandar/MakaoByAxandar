package com.axandar.makaoCore.logic;

import com.axandar.makaoCore.utils.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 25.01.2016.
 */
public class Player implements Serializable{

    private String playerName;
    private String playerIP;
    private int playerID;
    private boolean isEndedGame;
    private boolean isMakao;
    private boolean wasPuttedCard;
    private int toWaitTurns;
    private List<Card> cardsInHand = new ArrayList<>();

    public Player(String _playerName, String _playerIP, int _playerID){
        playerName = _playerName;
        playerIP = _playerIP;
        playerID = _playerID;
        isEndedGame = false;
        isMakao = false;
        wasPuttedCard = false;
        toWaitTurns = 0;
    }

    public String getPlayerName(){
        return playerName;
    }

    public void setPlayerName(String playerName){
        this.playerName = playerName;
    }

    public String getPlayerIP(){
        return playerIP;
    }

    public void setPlayerIP(String playerIP){
        this.playerIP = playerIP;
    }

    public int getPlayerID(){
        return playerID;
    }

    public void setPlayerID(int playerID){
        this.playerID = playerID;
    }

    public boolean isEndedGame(){
        return isEndedGame;
    }

    public void setEndedGame(boolean endedGame){
        isEndedGame = endedGame;
    }

    public boolean isMakao(){
        return isMakao;
    }

    public void setMakao(boolean makao){
        isMakao = makao;
    }

    public List<Card> getCardsInHand(){
        return cardsInHand;
    }

    public void setCardsInHand(List<Card> cardsInHand){
        this.cardsInHand = cardsInHand;
    }

    public void removeCardFromHand(Card card){
        Logger.logConsole("Operation on player", "Removed card: " + card.getIdType() + "-" + card.getIdColor());
        int index = -1;
        for(Card cardInHand : cardsInHand){
            if(cardInHand.getIdColor() == card.getIdColor() && cardInHand.getIdType() == card.getIdType()){
                index = cardsInHand.indexOf(cardInHand);
                break;
            }
        }
        cardsInHand.remove(index);
    }

    public void addCardToHand(Card card){
        cardsInHand.add(card);
    }

    public boolean wasPuttedCard(){
        return wasPuttedCard;
    }

    public void setWasPuttedCard(boolean wasPuttedCard){
        this.wasPuttedCard = wasPuttedCard;
    }

    public int getToWaitTurns(){
        return toWaitTurns;
    }

    public void setToWaitTurns(int toWaitTurns){
        this.toWaitTurns = toWaitTurns;
    }
}
