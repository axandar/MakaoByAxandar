package com.axandar.makaoClient;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.logic.StopMakao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 12.03.2016.
 */
public class ClientProperties{

    private String nickname;
    private String ip;
    private int port;
    private boolean isClientRunning = false;
    private List<Player> additionalPlayers = new ArrayList<>();
    private int estimatedPlayersNumber;
    private Player localPlayer;
    private Card cardOnTop;
    private List<Card> puttedCards = new ArrayList<>();
    private boolean isSaidMakao = false;
    private StopMakao stopMakao;
    private boolean isTurnEnded = false;
    private List<Card> cardsToPut = new ArrayList<>();
    private Card orderedCard;
    private List<Card> notAcceptedCards = new ArrayList<>();
    private boolean isMakao = false;
    private boolean isGameEnded = false;
    private boolean isUpdateGame = false;
    private boolean isCardsRejected = false;
    private boolean isTurnStarted = false;
    private List<Integer> suitableCardsToOrder = new ArrayList<>();

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

    public List<Player> getAdditionalPlayers(){
        return additionalPlayers;
    }

    public void setAdditionalPlayers(List<Player> additionalPlayers){
        this.additionalPlayers = additionalPlayers;
    }

    public void addPlayer(Player player){
        additionalPlayers.add(player);
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

    public boolean isSaidMakao(){
        return isSaidMakao;
    }

    public void setSaidMakao(boolean saidMakao){
        isSaidMakao = saidMakao;
    }

    public StopMakao getStopMakao(){
        return stopMakao;
    }

    public void setStopMakao(StopMakao stopMakao){
        this.stopMakao = stopMakao;
    }

    public boolean isTurnEnded(){
        return isTurnEnded;
    }

    public void setTurnEnded(boolean turnEnded){
        isTurnEnded = turnEnded;
    }

    public List<Card> getCardsToPut(){
        return cardsToPut;
    }

    public void setCardsToPut(List<Card> cardsToPut){
        this.cardsToPut = cardsToPut;
    }

    public void addCardToPut(Card card){
        cardsToPut.add(card);
    }

    public Card getOrderedCard(){
        return orderedCard;
    }

    public void setOrderedCard(Card orderedCard){
        this.orderedCard = orderedCard;
    }

    public List<Card> getNotAcceptedCards(){
        return notAcceptedCards;
    }

    public void setNotAcceptedCards(List<Card> notAcceptedCards){
        this.notAcceptedCards = notAcceptedCards;
    }

    public void addNotAcceptedCard(Card card){
        notAcceptedCards.add(card);
    }

    public boolean isMakao(){
        return isMakao;
    }

    public void setMakao(boolean makao){
        isMakao = makao;
    }

    public boolean isGameEnded(){
        return isGameEnded;
    }

    public void setGameEnded(boolean gameEnded){
        isGameEnded = gameEnded;
    }

    public boolean isUpdateGame(){
        return isUpdateGame;
    }

    public void setUpdateGame(boolean updateGame){
        isUpdateGame = updateGame;
    }

    public boolean isCardsRejected(){
        return isCardsRejected;
    }

    public void setCardsRejected(boolean cardsRejected){
        isCardsRejected = cardsRejected;
    }

    public boolean isTurnStarted(){
        return isTurnStarted;
    }

    public void setTurnStarted(boolean turnStarted){
        isTurnStarted = turnStarted;
    }

    public List<Integer> getSuitableCardsToOrder(){
        return suitableCardsToOrder;
    }

    public void addSuitableCardsToOrder(Integer suitableCardsToOrder){
        this.suitableCardsToOrder.add(suitableCardsToOrder);
    }

    public void setSuitableCardsToOrder(List<Integer> suitableCardsToOrder){
        this.setSuitableCardsToOrder(suitableCardsToOrder);
    }
}
