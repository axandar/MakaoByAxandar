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

    public static List<List<Function>> functionsList;
    private int numberOfDecks;
    private int numberOfPlayers;

    private SessionInfo sessionInfo;

    public TableServer(SessionInfo sessionInfo, int _numberOfPlayers, int _numberOfDecks,
                       List<List<Function>> _functionList) {
        this.sessionInfo = sessionInfo;
        numberOfDecks = _numberOfDecks;
        numberOfPlayers = _numberOfPlayers;
        functionsList = _functionList;
    }

    public void initializeGame(){
        sessionInfo.setDeckOnTable(new Deck(numberOfDecks, functionsList));
        sessionInfo.setGraveyard(new Deck());

        sessionInfo.setCardOnTop(sessionInfo.getDeckOnTable().getCardFromDeck());
        Logger.logConsole(TAG, "Card on top: " + sessionInfo.getCardOnTop().getIdType()
                + "-" + sessionInfo.getCardOnTop().getIdColor());
        givePlayersCards();

        int firstPlayerID =  ThreadLocalRandom.current().nextInt(0, numberOfPlayers);
        Logger.logConsole(TAG, "First player id: " + firstPlayerID);

        Player firstPlayer = sessionInfo.getPlayerObject(firstPlayerID);
        sessionInfo.setActualTurnPlayer(firstPlayer);
        int lastPlayerId = sessionInfo.getPreviousPlayerId(firstPlayer);
        sessionInfo.setLastTurnEndedPlayer(sessionInfo.getPlayerObject(lastPlayerId));

        sessionInfo.setTable(this);//update object for all players
        sessionInfo.setGameStarted(true);// TODO: 11.04.2016 Wszystko jest co potrzebne?
    }

    private void givePlayersCards(){
        for (Player player: sessionInfo.getPlayersObjectsInOrder()) {
            List<Card> cardsToAdd = new ArrayList<>();
            for(int i = 0; i < 5; i++){
                cardsToAdd.add(sessionInfo.getDeckOnTable().getCardFromDeck());
            }
            player.setCardsInHand(cardsToAdd);
        }
    }

    public boolean putCardOnTable(Card card){
        if(sessionInfo.getOrderedCard() == null && sessionInfo.getQuantityCardsToTake() == 0
                && sessionInfo.getQuantityTurnsToWait() == 0){
            if(isTypeCorrectly(card) || isColorCorrectly(card) ||
                    (card.getFunction().getFunctionID() == Function.CAMELEON_CARD
                            || sessionInfo.getCardOnTop().getFunction().getFunctionID() == Function.CAMELEON_CARD)){
                putCard(card);
                return true;
            }else return false;
        }else if(sessionInfo.getOrderedCard() != null && sessionInfo.getQuantityTurnsToWait() == 0
                && sessionInfo.getQuantityCardsToTake() == 0){
            if(sessionInfo.getOrderingCard().getFunction().getFunctionID() == card.getFunction().getFunctionID() &&
                    (isColorCorrectly(card) || isTypeCorrectly(card))){
                putCard(card);
                return true;
            }else if(sessionInfo.getOrderingCard().getFunction().getFunctionID() == Function.CHANGE_COLOR
                    && isColorCorrectly(card)){
                putCard(card);
                return true;
            }else if(sessionInfo.getOrderingCard().getFunction().getFunctionID() == Function.ORDER_CARD
                    && isTypeCorrectly(card)){
                putCard(card);
                return true;
            }else return false;
        }else if(sessionInfo.getQuantityTurnsToWait() != 0){
            if(card.getFunction().getFunctionID() == Function.WAIT_TURNS &&
                    (isTypeCorrectly(card) || isColorCorrectly(card))){
                putCard(card);
                return true;
            }else return false;
        }else if(sessionInfo.getQuantityCardsToTake() != 0){
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
            sessionInfo.setOrderedCard(_orderedCard);
            sessionInfo.setOrderingCard(card);
            return true;
        }else return false;
    }

    private void putCard(Card card){
        if(card.getFunction().getFunctionID() == Function.GET_CARDS_FORWARD){
            sessionInfo.setQuantityCardsToTake(sessionInfo.getQuantityCardsToTake()
                    + card.getFunction().getFunctionValue());
            sessionInfo.setNextPlayerForward(true);
        }else if(card.getFunction().getFunctionID() == Function.GET_CARDS_BACKWARD){
            sessionInfo.setQuantityCardsToTake(sessionInfo.getQuantityCardsToTake()
                    + card.getFunction().getFunctionValue());
            sessionInfo.setNextPlayerForward(false);
        }else if(card.getFunction().getFunctionID() == Function.WAIT_TURNS){
            sessionInfo.setQuantityTurnsToWait(sessionInfo.getQuantityTurnsToWait()
                    + card.getFunction().getFunctionValue());
            sessionInfo.setNextPlayerForward(true);
        }else{
            sessionInfo.setNextPlayerForward(true);
        }
        sessionInfo.setCardOnTop(card);
    }

    public Player giveCardToPlayer(Player player, int quantity){
        for(int i = 0; i < quantity; i++){
            if(sessionInfo.getDeckOnTable().deckLength() == 0){
                sessionInfo.setDeckOnTable(sessionInfo.getGraveyard());
                sessionInfo.setGraveyard(new Deck());
            }
            player.addCardToHand(sessionInfo.getDeckOnTable().getCardFromDeck());
        }
        return player;
    }

    public Player setPlayerToWaitTurns(Player player, int quantity){
        player.setToWaitTurns(quantity);
        return player;
    }

    private boolean isColorCorrectly(Card card) {
        return sessionInfo.getCardOnTop().getIdColor() == card.getIdColor();
    }

    private boolean isTypeCorrectly(Card card) {
        return sessionInfo.getCardOnTop().getIdType() == card.getIdType();
    }

    public void endTurn(Player player){
        // TODO: 22.04.2016 complication with players id??
        if(player.isMakao()){
            sessionInfo.getPlayersObjectsInOrder().remove(player);
            if(sessionInfo.getPlayersObjectsInOrder().size() == 1){
                endGame();
            }
        }
        if(sessionInfo.isNextPlayerForward()){
            int nextPlayerId = sessionInfo.getNextPlayerId(player);
            Player nextPlayer = sessionInfo.getPlayersObjectsInOrder().get(nextPlayerId);
            while(nextPlayer.getToWaitTurns() != 0){
                nextPlayer.setToWaitTurns(nextPlayer.getToWaitTurns() - 1);
                int nextNextPlayerId = sessionInfo.getNextPlayerId(player);
                nextPlayer = sessionInfo.getPlayersObjectsInOrder().get(nextNextPlayerId);
            }
            sessionInfo.setLastTurnEndedPlayer(player);
            sessionInfo.setActualTurnPlayer(nextPlayer);
        }else{
            int previousPlayerId = sessionInfo.getPreviousPlayerId(player);
            Player previousPlayer = sessionInfo.getPlayersObjectsInOrder().get(previousPlayerId);
            while(previousPlayer.getToWaitTurns() != 0){
                int nextPreviousPlayerId = sessionInfo.getPreviousPlayerId(player);
                previousPlayer = sessionInfo.getPlayersObjectsInOrder().get(nextPreviousPlayerId);
            }
            sessionInfo.setNextPlayerForward(true);
            sessionInfo.setLastTurnEndedPlayer(player);
            sessionInfo.setActualTurnPlayer(previousPlayer);
        }

    }


    private void endGame(){
        sessionInfo.setGameExiting(true);
    }
}