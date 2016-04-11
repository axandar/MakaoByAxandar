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
        Logger.logConsole("server initializing", "first player id: " + firstPlayerID);
        sessionInfo.setActualTurnPlayer(sessionInfo.getPlayerObject(firstPlayerID));
        sessionInfo.setLastTurnEndedPlayer(sessionInfo.getFirstPlayer());
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
            isNextPlayerFroward = true;
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


    private void endGame(){
        sessionInfo.setGameExited(true);
    }
}