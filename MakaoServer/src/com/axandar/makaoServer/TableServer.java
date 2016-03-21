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
    private int quanityTurnsToWait = 0;
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
        givePlayersCards();
        //updatePlayersInfo(); // TODO: 15.03.2016 raczej niepotrzebne???

        int firstPlayerID =  ThreadLocalRandom.current().nextInt(0, players.size());
        Logger.logConsole("server initializing", "first player id: " + firstPlayerID);
        actualPlayer = players.get(firstPlayerID);// TODO: 14.02.2016 dodac losowa kolejnosc graczy w liscie
        sessionInfo.setJustEndedTurnPlayerId(actualPlayer.getPlayerID());
        sessionInfo.setGameStarted(true);
        // TODO: 24.02.2016 inicjalizacja zmienne justEndedTurnPlayerId w sessionInfo na pierwszego grcza w tabeli
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

    public boolean putCardOnTable(Card card, Player player){
        if(isFunctionCorrectly(card, Function.CAMELEON_CARD)){
            graveyard.addCardToDeck(cardOnTop);
            cardOnTop = card;
            player.setWasPuttedCard(true);
            return true;
        }else if(isFunctionCorrectly(card, Function.ORDER_CARD)){
            if(isTypeCorrectly(card)){
                graveyard.addCardToDeck(cardOnTop);
                cardOnTop = card;
                player.setWasPuttedCard(true);
                return true;
            }else return false;
        }else if(isFunctionCorrectly(card, Function.CHANGE_COLOR)){
            if(isColorCorrectly(card)){
                graveyard.addCardToDeck(cardOnTop);
                cardOnTop = card;
                player.setWasPuttedCard(true);
                return true;
            }else return false;
        }else if(isFunctionCorrectly(card, Function.GET_CARDS_BACKWARD)
                || (isFunctionCorrectly(card, Function.GET_CARDS_FORWARD))){
            if(isTypeCorrectly(card) || isColorCorrectly(card)){
                graveyard.addCardToDeck(cardOnTop);
                cardOnTop = card;
                player.setWasPuttedCard(true);
                quantityCardsToTake += card.getFunction().getFunctionValue();
                if(isFunctionCorrectly(card, Function.GET_CARDS_BACKWARD)){
                    isNextPlayerFroward = false;
                }
                return true;
            }else return false;
        }else if(isFunctionCorrectly(card, Function.WAIT_TURNS)){
            if(isTypeCorrectly(card) || isColorCorrectly(card)){
                graveyard.addCardToDeck(cardOnTop);
                cardOnTop = card;
                player.setWasPuttedCard(true);
                quanityTurnsToWait += card.getFunction().getFunctionValue();
                return true;
            }else return false;
        }else if(isFunctionCorrectly(card, Function.NOTHING)){
            if(isTypeCorrectly(card) || isColorCorrectly(card)){
                graveyard.addCardToDeck(cardOnTop);
                cardOnTop = card;
                player.setWasPuttedCard(true);
                return true;
            }else return false;
        }else return false;
        // TODO: 18.02.2016 dodac warunek gdy jest zadanie kart ??specjalna funkcja karty jako zazadna??
    }

    public boolean putOrderCardOnTable(Card card, Card _orderedCard, Player player){
        if(putCardOnTable(card, player)){
            // TODO: 21.03.2016 exception when ordered card have functions
            orderedCard = _orderedCard;
            return true;
        }else return false;
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

    private boolean isFunctionCorrectly(Card card, int function){
        return (cardOnTop.getFunction().getFunctionID() == function)
                && (card.getFunction().getFunctionID() == function);
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
        // TODO: 22.02.2016 dla kazdego polaczenia wysylanie zaktualizowanej listy graczy
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

    public void setQuantityCardsToTake(int quantityCardsToTake){
        this.quantityCardsToTake = quantityCardsToTake;
    }

    public int getQuantityTurnsToWait(){
        return quanityTurnsToWait;
    }

    public void setQuantityTurnsToWait(int quanityTurnsToWait){
        this.quanityTurnsToWait = quanityTurnsToWait;
    }

    public Card getCardOnTop(){
        return cardOnTop;
    }
}