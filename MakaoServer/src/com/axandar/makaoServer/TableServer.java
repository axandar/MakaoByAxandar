package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;
import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Deck;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Axandar on 25.01.2016.
 */
public class TableServer {

    private final String TAG = "TableServer on server";

    // TODO: 15.03.2016 refactoring
    public static int NUMBER_OF_DECKS = 0;
    public static List<List<Function>> functionsList;

    private SessionInfo sessionInfo;

    private Player actualPlayer;
    private List<Player> players;
    private Deck graveyard = new Deck();
    private Deck deck;
    private Card cardOnTop;
    private Card orderedCard;
    private int quantityCardsToTake = 0;
    private int quantityTurnsToWait = 0;
    private boolean isNextPlayerFroward = true;

    public TableServer() {

    }

    public void initializeGame(int _numberOfDecks, List<Player> _players, List<List<Function>> _functionsList,
                               SessionInfo _sessionInfo){
        sessionInfo = _sessionInfo;

        NUMBER_OF_DECKS = _numberOfDecks;
        functionsList = _functionsList;
        players = _players;

        deck = new Deck(NUMBER_OF_DECKS, functionsList);

        cardOnTop = deck.getCardFromDeck();
        Logger.logConsole(TAG, "Card on top: " + cardOnTop.getIdType() + "-" + cardOnTop.getIdColor());
        givePlayersCards();

        int firstPlayerID =  ThreadLocalRandom.current().nextInt(0, players.size());
        Logger.logConsole("server initializing", "first player id: " + firstPlayerID);
        actualPlayer = players.get(firstPlayerID);
        sessionInfo.setJustEndedTurnPlayerId(actualPlayer.getPlayerID());
        sessionInfo.setGameStarted(true);
    }

    private void givePlayersCards(){
        for (Player player: players) {
            List<Card> cardsToAdd = new ArrayList<>();
            for(int i = 0; i < 5; i++){
                cardsToAdd.add(deck.getCardFromDeck());
            }
            player.setCardsInHand(cardsToAdd);
        }
    }

    public boolean putCardOnTable(Card card){
        if(orderedCard == null && quantityCardsToTake == 0 && quantityTurnsToWait == 0){
            if(isTypeCorrectly(card) || isColorCorrectly(card) ||
                    (card.getFunction().getFunctionID() == Function.CAMELEON_CARD
                            || cardOnTop.getFunction().getFunctionID() == Function.CAMELEON_CARD)){
                putCard(card);
                return true;
            }else return false;
        }else if(orderedCard != null && quantityTurnsToWait == 0 && quantityCardsToTake == 0){
            if(cardOnTop.getFunction().getFunctionID() == card.getFunction().getFunctionID() &&
                    (isColorCorrectly(card) || isTypeCorrectly(card))){
                putCard(card);
                return true;
            }else if(cardOnTop.getFunction().getFunctionID() == Function.CHANGE_COLOR && isColorCorrectly(card)){
                putCard(card);
                return true;
            }else if(cardOnTop.getFunction().getFunctionID() == Function.ORDER_CARD && isTypeCorrectly(card)){
                putCard(card);
                return true;
            }else return false;
        }else if(quantityTurnsToWait != 0){
            if(card.getFunction().getFunctionID() == Function.WAIT_TURNS &&
                    (isTypeCorrectly(card) || isColorCorrectly(card))){
                putCard(card);
                return true;
            }else return false;
        }else if(quantityCardsToTake != 0){
            if(card.getFunction().getFunctionID() == Function.GET_CARDS_FORWARD &&
                    (isTypeCorrectly(card) || isColorCorrectly(card))){
                putCard(card);
                return true;
            }else if(card.getFunction().getFunctionID() == Function.GET_CARDS_BACKWARD &&
                    (isTypeCorrectly(card) || isColorCorrectly(card))){
                putCard(card);
                return true;
            }else return false;
        }else{
            Logger.logConsole("TableServer: ", "Error in putting card on table");
            return false;
        }
    }

    public boolean putOrderCardOnTable(Card card, Card _orderedCard){
        if(putCardOnTable(card)){
            orderedCard = _orderedCard;
            return true;
        }else return false;
    }

    private void putCard(Card card){
        if(card.getFunction().getFunctionID() == Function.GET_CARDS_FORWARD){
            quantityCardsToTake += card.getFunction().getFunctionValue();
            isNextPlayerFroward = true;
        }else if(card.getFunction().getFunctionID() == Function.GET_CARDS_BACKWARD){
            quantityCardsToTake += card.getFunction().getFunctionValue();
            isNextPlayerFroward = false;
        }else if(card.getFunction().getFunctionID() == Function.WAIT_TURNS){
            quantityTurnsToWait += card.getFunction().getFunctionValue();
            isNextPlayerFroward = true;
        }else{
            Logger.logConsole(TAG, "Error in putting card");
        }
        cardOnTop = card;
    }

    public void giveCardToPlayer(Player player, int quanity){
        for(int i = 0; i < quanity; i++){
            if(deck.deckLength() == 0){
                deck = graveyard;
                graveyard = new Deck();
            }
            player.addCardToHand(deck.getCardFromDeck());
        }
    }

    public void setPlayerToWaitTurns(Player player, int quanity){
        player.setToWaitTurns(quanity);
    }

    private boolean isColorCorrectly(Card card) {
        return cardOnTop.getIdColor() == card.getIdColor();
    }

    private boolean isTypeCorrectly(Card card) {
        return cardOnTop.getIdType() == card.getIdType();
    }

    public void endTurn(Player player){
        if(player.isMakao()){
            players.remove(player);
            if(players.size() == 1){
                endGame();
            }
        }
        if(isNextPlayerFroward){
            Player nextPlayer = getNextPlayer(player);
            while(nextPlayer.getToWaitTurns() != 0){
                nextPlayer = getPreviousPlayer(nextPlayer);
            }
            isNextPlayerFroward = false;
            sessionInfo.setJustEndedTurnPlayerId(nextPlayer.getPlayerID());
            setActualPlayer(nextPlayer);
        }else{
            Player previousPlayer = getPreviousPlayer(player);
            while(previousPlayer.getToWaitTurns() != 0){
                previousPlayer.setToWaitTurns(previousPlayer.getToWaitTurns() - 1);
                previousPlayer = getPreviousPlayer(previousPlayer);
            }
            sessionInfo.setJustEndedTurnPlayerId(previousPlayer.getPlayerID());
            setActualPlayer(previousPlayer);
        }
    }

    public Player getNextPlayer(Player player){
        int requestedPlayerID = players.indexOf(player);
        int nextPlayerID;

        if(requestedPlayerID == players.size()-1){
            nextPlayerID = 0;
        }else nextPlayerID = requestedPlayerID + 1;

        Logger.logConsole("Server getNextPlayer()", "players size = " + players.size());
        Logger.logConsole("Server getNextPlayer()", "requestedPlayerID = " + requestedPlayerID);
        Logger.logConsole("Server getNextPlayer()", "nextPlayerID = " + nextPlayerID);
        return players.get(nextPlayerID);
    }

    private Player getPreviousPlayer(Player player){
        int requestedPlayerID = players.indexOf(player);
        int previousPlayerID;

        if(requestedPlayerID == players.size()-1){
            previousPlayerID = 0;
        }else previousPlayerID = requestedPlayerID + 1;

        Logger.logConsole("Server getPreviousPlayer()", "players size = " + players.size());
        Logger.logConsole("Server getPreviousPlayer()", "requestedPlayerID = " + requestedPlayerID);
        Logger.logConsole("Server getPreviousPlayer()", "previousPlayerID = " + previousPlayerID);

        return players.get(previousPlayerID);
    }

    private void endGame(){
        sessionInfo.setGameExited(true);
    }

    public Player getActualPlayer() {
        return actualPlayer;
    }

    public void setActualPlayer(Player actualPlayer) {
        this.actualPlayer = actualPlayer;
    }

    public int getQuantityCardsToTake(){
        return quantityCardsToTake;
    }

    public void setQuantityCardsToTakeToZero(){
        this.quantityCardsToTake = 0;
    }

    public int getQuantityTurnsToWait(){
        return quantityTurnsToWait;
    }

    public void setQuantityTurnsToWait(int quantityTurnsToWait){
        this.quantityTurnsToWait = quantityTurnsToWait;
    }

    public Card getCardOnTop(){
        return cardOnTop;
    }
}