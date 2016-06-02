package com.axandar.makaoClient.view;

import com.axandar.makaoClient.ClientProperties;
import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 27.05.2016.
 */
public abstract class GameMainViewController{

    protected final String TAG = "Client side";
    protected volatile ClientProperties properties;
    protected Player player;
    protected List<Card> cardsToPut = new ArrayList<>();
    protected List<Integer> suitableCardsTypeToPut = new ArrayList<>();
    protected Card orderedCard = null;
    protected Card ordered = null;
    protected List<Card> puttedCards = new ArrayList<>();

    private Runnable endTurnBtnLogin;
    private Runnable updateGUI;

    protected void initializeController(){
        Logger.logConsole(TAG, "Game started");

        updateGUI = this::updateGUI;

        endTurnBtnLogin = () -> {
            if(properties.isTurnStarted()){
                setGUIOnTurnActive();
            }else{
                setGUIOnTurnInactive();
            }
        };

        Task taskToUpdateGUI = new Task(){
            @Override
            protected Object call() throws Exception{
                while(!properties.isClientRunning()){
                    Thread.sleep(1000);
                    //waiting for client initialize
                }
                while(properties.isClientRunning()){
                    Platform.runLater(endTurnBtnLogin);
                    if(properties.isUpdateGame()){
                        Logger.logConsole(TAG, "Update added to runLater()");
                        Platform.runLater(updateGUI);
                        properties.setUpdateGame(false);
                    }
                    Platform.runLater(endTurnBtnLogin);
                    Thread.sleep(1000);
                }
                return null;
            }
        };
        Thread updatingGUI = new Thread(taskToUpdateGUI);
        updatingGUI.start();
    }

    private void updateGUI(){
        Logger.logConsole(TAG, "Start updating GUI");

        suitableCardsTypeToPut = properties.getSuitableCardsToOrder();//Numbers from Card class
        List<Card> notAcceptedCards = properties.getNotAcceptedCards();
        puttedCards = properties.getPuttedCards();

        properties.setNotAcceptedCards(new ArrayList<>());
        properties.setPuttedCards(new ArrayList<>());

        setOrderedCardImage();

        player = properties.getLocalPlayer();

        if(properties.getCardsToPut().size() > 0 && properties.getNotAcceptedCards().size() == 0){
            properties.setCardsToPut(new ArrayList<>());
        }else if(notAcceptedCards.size() > 0){
            for(Card card : notAcceptedCards){
                properties.getLocalPlayer().removeCardFromHand(card);
            }
        }

        putCardsWasGood();
        Logger.logConsole(TAG, "Started adding cards");
        clearHandFromCards();
        for(Card card : player.getCardsInHand()){
            addCardToHandGUI(card);
        }

        setCardOnTopTexture(properties.getCardOnTop());

        populateListOfAnotherPlayers();

        Logger.logConsole(TAG, "Update ended");
    }

    protected abstract void setGUIOnTurnActive();

    protected abstract void setGUIOnTurnInactive();

    protected abstract void setOrderedCardImage();

    private void putCardsWasGood(){
        if(properties.isCardsRejected() && properties.getNotAcceptedCards().size() > 0){
            Logger.logConsole(TAG, "Some cards not accepted");
            makeNotification("Wrong cards", "Some of cards that you tried to send was incorrect");
        }
    }

    protected abstract void clearHandFromCards();

    protected abstract void addCardToHandGUI(Card card);

    protected void handleClickOnCard(String cardViewContainerID){
        for(Card cardFromPlayer : player.getCardsInHand()){
            String cardName = cardFromPlayer.getIdType() + "-" + cardFromPlayer.getIdColor();
            if(cardViewContainerID.equals(cardName)){
                changeClickedCardState(cardViewContainerID, cardFromPlayer);
                Logger.logConsole(TAG, "Clicked card: " + cardName);
                break;
            }
        }
    }

    private void changeClickedCardState(String cardViewContainerID, Card cardFromPlayer){
        if(cardsToPut.contains(cardFromPlayer)){
            removeCardToPut(cardFromPlayer);
            cardClickedSecondTime(cardViewContainerID);
        }else{
            whichCardTypeClickedFirstTime(cardViewContainerID, cardFromPlayer);
        }
    }

    protected abstract void cardClickedSecondTime(String cardViewContainerID);

    protected void removeCardToPut(Card card){
        cardsToPut.remove(card);
    }

    protected void whichCardTypeClickedFirstTime(String cardViewContainerID, Card cardFromPlayer){
        if(cardFromPlayer.getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                cardFromPlayer.getFunction().getFunctionID() == Function.ORDER_CARD){

            clickedOrderCardFirstTime(cardViewContainerID, cardFromPlayer);
        }else{
            clickedNormalCardFirstTime(cardViewContainerID);
        }
        cardsToPut.add(cardFromPlayer);
    }

    protected abstract void clickedOrderCardFirstTime(String cardViewContainerID, Card cardFromPlayer);

    protected abstract void clickedNormalCardFirstTime(String cardViewContainerID);

    protected abstract void makeNotification(String title, String text);

    protected abstract void setCardOnTopTexture(Card card);

    protected abstract void populateListOfAnotherPlayers();

    protected void handleSayMakao(){
        if(properties.getLocalPlayer().getCardsInHand().size() == 1 && !properties.getLocalPlayer().isMakao()){
            properties.getLocalPlayer().setMakao(true);
            setSayMakao(true);
        }else if(properties.getLocalPlayer().isMakao()){
            properties.getLocalPlayer().setMakao(false);
            setSayMakao(false);
        }
    }

    protected abstract void setSayMakao(boolean isMakao);

    protected void handleEndTurn(){
        if(cardsToPut.size() > 0){
            Logger.logConsole(TAG, "Requested sending cards");
            properties.setCardsToPut(cardsToPut);
            if(cardsToPut.get(0).getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                    cardsToPut.get(0).getFunction().getFunctionID() == Function.ORDER_CARD){
                properties.setOrderedCard(orderedCard);
            }
            cardsToPut = new ArrayList<>();
        }
        setOrderedCardImage();
        properties.setTurnStarted(false);
        properties.setTurnEnded(true);
        Logger.logConsole(TAG, "Ended turn");
    }
}
