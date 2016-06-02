package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Deck;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Axandar on 25.01.2016.
 */
public class TableServer{

    private final String TAG = "TableServer on server";

    public static List<List<Function>> functionsList;
    private int numberOfDecks;
    private int numberOfPlayers;

    private SessionInfo sessionInfo;

    public TableServer(SessionInfo sessionInfo, int _numberOfPlayers, int _numberOfDecks,
                       List<List<Function>> _functionList){
        this.sessionInfo = sessionInfo;
        numberOfDecks = _numberOfDecks;
        numberOfPlayers = _numberOfPlayers;
        functionsList = _functionList;
    }

    public void initializeGame(){
        sessionInfo.setDeckOnTable(new Deck(numberOfDecks, functionsList));
        setCardsProperlyToOrder();
        sessionInfo.setGraveyard(new Deck());

        sessionInfo.setCardOnTop(sessionInfo.getDeckOnTable().getCardFromDeck());
        Logger.logConsole(TAG, "Card on top: " + sessionInfo.getCardOnTop().getIdType()
                + "-" + sessionInfo.getCardOnTop().getIdColor());
        givePlayersCards();

        int firstPlayerID = ThreadLocalRandom.current().nextInt(0, numberOfPlayers);
        Logger.logConsole(TAG, "First player id: " + firstPlayerID);

        Player firstPlayer = sessionInfo.getPlayerObject(firstPlayerID);
        sessionInfo.setActualTurnPlayer(firstPlayer);
        sessionInfo.setLastTurnEndedPlayer(new Player(null, null, -1));

        sessionInfo.setTable(this);
        sessionInfo.setGameStarted(true);
    }

    private void setCardsProperlyToOrder(){
        int index = 1; //Types are starting from 1
        for(List<Function> functionList : functionsList){
            int i = 0;
            for(Function fucntion : functionList){
                if(fucntion.getFunctionID() == Function.NOTHING){
                    i++;
                }
            }
            if(i == 4){
                sessionInfo.addSuitableCardsToOrder(index);
            }
            index++;
        }
    }

    private void givePlayersCards(){
        for(Player player : sessionInfo.getPlayersObjectsInOrder()){
            List<Card> cardsToAdd = new ArrayList<>();
            for(int i = 0; i < 5; i++){
                cardsToAdd.add(sessionInfo.getDeckOnTable().getCardFromDeck());
            }
            //Debug only
            cardsToAdd.add(new Card(0, 4, new Function(2, 1)));
            cardsToAdd.add(new Card(0, 5, new Function(6, -1)));
            cardsToAdd.add(new Card(0, 12, new Function(4, -1)));
            cardsToAdd.add(new Card(0, 11, new Function(Function.ORDER_CARD, -1)));
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
                    && isOrderColorCorrectly(card)){
                putCard(card);
                return true;
            }else if(sessionInfo.getOrderingCard().getFunction().getFunctionID() == Function.ORDER_CARD
                    && isOrderTypeCorrectly(card)){
                putCard(card);
                return true;
            }else if(sessionInfo.getOrderingCard().getFunction().getFunctionValue() == 0 &&
                    isColorCorrectly(card) || isTypeCorrectly(card)){
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
            Logger.logConsole(TAG, "Error in putting card on table");
            return false;
        }
    }

    public boolean putOrderCardOnTable(Card card, Card _orderedCard){
        if(putCardOnTable(card)){
            sessionInfo.setOrderedCard(_orderedCard);
            sessionInfo.setOrderingCard(card);
            if(card.getFunction().getFunctionID() == Function.ORDER_CARD){
                sessionInfo.setTurnCounter(sessionInfo.getNumberOfPlayers() + 1);
            }
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

        if(sessionInfo.getOrderedCard() != null
                && sessionInfo.getOrderingCard().getFunction().getFunctionID() == Function.CHANGE_COLOR){
            Logger.logConsole(TAG, "Clearing ordering state after order color");
            sessionInfo.setOrderingCard(new Card(-1, -1, new Function(Function.ORDERED,0)));
            sessionInfo.setOrderedCard(new Card(-1, -1, new Function(Function.ORDERED,0)));
        }

        sessionInfo.addLastPlacedCard(card);
        sessionInfo.addCardToGraveyard(sessionInfo.getCardOnTop());
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

    private boolean isColorCorrectly(Card card){
        return sessionInfo.getCardOnTop().getIdColor() == card.getIdColor();
    }

    private boolean isTypeCorrectly(Card card){
        return sessionInfo.getCardOnTop().getIdType() == card.getIdType();
    }

    private boolean isOrderColorCorrectly(Card card){
        return sessionInfo.getOrderedCard().getIdColor() == card.getIdColor();
    }

    private boolean isOrderTypeCorrectly(Card card){
        return sessionInfo.getOrderedCard().getIdType() == card.getIdType();
    }

    public void endTurn(Player player){
        if(sessionInfo.getOrderedCard() != null){
            Logger.logConsole(TAG, "Checking counter");
            sessionInfo.setTurnCounter(sessionInfo.getTurnCounter() - 1);
            if(sessionInfo.getTurnCounter() == 0){
                Logger.logConsole(TAG, "Counter for order ended");
                sessionInfo.setOrderedCard(new Card(-1, -1, new Function(Function.ORDERED, 0)));
                //for sending empty card to disable ordered view on client
                sessionInfo.setOrderingCard(new Card(-1, -1, new Function(Function.ORDERED, 0)));
            }
        }

        if(player.isMakao()){
            sessionInfo.getPlayersObjectsInOrder().remove(player);
            if(sessionInfo.getPlayersObjectsInOrder().size() == 1){
                endGame();
            }
        }
        if(sessionInfo.isNextPlayerForward()){
            takeForwardPlayer(player);
        }else{
            takeBackwardPlayer(player);
        }
    }

    private void takeForwardPlayer(Player player){
        sessionInfo.setNumberOfWaitingPlayers(0);
        int nextPlayerId = sessionInfo.getNextPlayerIndex(player);
        Player nextPlayer = sessionInfo.getPlayersObjectsInOrder().get(nextPlayerId);
        while(nextPlayer.getToWaitTurns() != 0){
            nextPlayer.setToWaitTurns(nextPlayer.getToWaitTurns() - 1);
            int nextNextPlayerId = sessionInfo.getNextPlayerIndex(nextPlayer);
            nextPlayer = sessionInfo.getPlayersObjectsInOrder().get(nextNextPlayerId);
            sessionInfo.setNumberOfWaitingPlayers(sessionInfo.getNumberOfWaitingPlayers() + 1);
        }
        sessionInfo.setLastTurnEndedPlayer(player);
        Logger.logConsole(TAG, "Player with id: " + nextPlayer.getPlayerID() + " is next player");
        sessionInfo.setActualTurnPlayer(nextPlayer);
    }

    private void takeBackwardPlayer(Player player){
        int previousPlayerId = sessionInfo.getPreviousPlayerIndex(player);
        Player previousPlayer = sessionInfo.getPlayersObjectsInOrder().get(previousPlayerId);
        while(previousPlayer.getToWaitTurns() != 0){
            int nextPreviousPlayerId = sessionInfo.getPreviousPlayerIndex(player);
            previousPlayer = sessionInfo.getPlayersObjectsInOrder().get(nextPreviousPlayerId);
        }
        sessionInfo.setNextPlayerForward(true);
        sessionInfo.setLastTurnEndedPlayer(player);
        sessionInfo.setActualTurnPlayer(previousPlayer);
    }

    private void endGame(){
        sessionInfo.setGameExiting(true);
    }
}