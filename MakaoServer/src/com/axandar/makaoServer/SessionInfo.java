package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Deck;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoServer.ErrorHandling.ConnectError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 14.02.2016.
 */
public class SessionInfo {


    private int playersNotReady = 0;
    private List<Player> playersObjectsInOrder = new ArrayList<>();
    private List<Card> lastPlacedCards = new ArrayList<>();
    private List<ConnectError> connectionErrors;
    private Card cardOnTop;
    private Card orderingCard;
    private Card orderedCard;
    private int quantityTurnsToWait = 0;
    private int quantityCardsToTake = 0;
    private boolean isNextPlayerForward;
    private StopMakao lastSaid;
    private Player lastTurnEndedPlayer;
    private Player actualTurnPlayer;
    private boolean isGameStarted;
    private boolean isGameExiting;
    private Deck graveyard;
    private Deck deckOnTable;
    private TableServer table;

    public int getPlayersNotReady(){
        return playersNotReady;
    }

    public void setPlayersNotReady(int playersNotReady){
        this.playersNotReady = playersNotReady;
    }

    public void increasePlayersNotReady(){
        playersNotReady++;
    }

    public List<Player> getPlayersObjectsInOrder(){
        return playersObjectsInOrder;
    }

    public Player getPlayerObject(int index){
        return playersObjectsInOrder.get(index);
    }

    public void setPlayersObjectsInOrder(List<Player> playersObjectsInOrder){
        this.playersObjectsInOrder = playersObjectsInOrder;
    }

    public void addPlayerObjectToList(Player player){
        playersObjectsInOrder.add(player);
    }

    public void removePlayerFromList(Player player){
        playersObjectsInOrder.remove(player);
    }

    public int getNextPlayerId(Player player){
        if(playersObjectsInOrder.indexOf(player) == playersObjectsInOrder.size()){
            return 0;
        }else return playersObjectsInOrder.indexOf(player) + 1;
    }

    public int getPreviousPlayerId(Player player){
        if(playersObjectsInOrder.indexOf(player) == 0){
            return playersObjectsInOrder.size();
        }else return playersObjectsInOrder.indexOf(player);
    }

    public List<Card> getLastPlacedCards(){
        return lastPlacedCards;
    }

    public void setLastPlacedCards(List<Card> lastPlacedCards){
        this.lastPlacedCards = lastPlacedCards;
    }

    public List<ConnectError> getConnectionErrors(){
        return connectionErrors;
    }

    public void setConnectionErrors(List<ConnectError> connectionErrors){
        this.connectionErrors = connectionErrors;
    }

    public void addConnectionError(ConnectError error){
        connectionErrors.add(error);
    }

    public void removeConnectionError(ConnectError error){
        connectionErrors.remove(error);
    }

    public Card getCardOnTop(){
        return cardOnTop;
    }

    public void setCardOnTop(Card cardOnTop){
        this.cardOnTop = cardOnTop;
    }

    public Card getOrderingCard(){
        return orderingCard;
    }

    public void setOrderingCard(Card orderingCard){
        this.orderingCard = orderingCard;
    }

    public Card getOrderedCard(){
        return orderedCard;
    }

    public void setOrderedCard(Card orderedCard){
        this.orderedCard = orderedCard;
    }

    public int getQuantityTurnsToWait(){
        return quantityTurnsToWait;
    }

    public void setQuantityTurnsToWait(int quantityTurnsToWait){
        this.quantityTurnsToWait = quantityTurnsToWait;
    }

    public void increaseTurnsToWaitQuantity(int value){
        quantityTurnsToWait += value;
    }

    public int getQuantityCardsToTake(){
        return quantityCardsToTake;
    }

    public void setQuantityCardsToTake(int quantityCardsToTake){
        this.quantityCardsToTake = quantityCardsToTake;
    }

    public void increaseCardsToTakeQuantity(int value){
        quantityCardsToTake += value;
    }

    public boolean isNextPlayerForward(){
        return isNextPlayerForward;
    }

    public void setNextPlayerForward(boolean nextPlayerForWard){
        isNextPlayerForward = nextPlayerForWard;
    }

    public StopMakao getLastSaid(){
        return lastSaid;
    }

    public void setLastSaid(StopMakao lastSaid){
        this.lastSaid = lastSaid;
    }

    public Player getLastTurnEndedPlayer(){
        return lastTurnEndedPlayer;
    }

    public void setLastTurnEndedPlayer(Player lastTurnEndedPlayer){
        this.lastTurnEndedPlayer = lastTurnEndedPlayer;
    }

    public Player getActualTurnPlayer(){
        return actualTurnPlayer;
    }

    public void setActualTurnPlayer(Player actualTurnPlayer){
        this.actualTurnPlayer = actualTurnPlayer;
    }

    public boolean isGameStarted(){
        return isGameStarted;
    }

    public void setGameStarted(boolean gameStarted){
        isGameStarted = gameStarted;
    }

    public boolean isGameExiting(){
        return isGameExiting;
    }

    public void setGameExiting(boolean gameExiting){
        isGameExiting = gameExiting;
    }

    public Deck getGraveyard(){
        return graveyard;
    }

    public void setGraveyard(Deck graveyard){
        this.graveyard = graveyard;
    }

    public void addCardToGraveyard(Card card){
        graveyard.addCardToDeck(card);
    }

    public Deck getDeckOnTable(){
        return deckOnTable;
    }

    public void setDeckOnTable(Deck deckOnTable){
        this.deckOnTable = deckOnTable;
    }

    public void addCardToDeckOnTable(Card card){
        deckOnTable.addCardToDeck(card);
    }

    public List<Card> takeCardsFromDeckTop(int quantity){
        List<Card> listOfCard = new ArrayList<>();
        for(int i = 0; i < quantity; i++){
            listOfCard.add(deckOnTable.getCardFromDeck());
        }
        return listOfCard;
    }

    public TableServer getTable(){
        return table;
    }

    public void setTable(TableServer table){
        this.table = table;
    }
}