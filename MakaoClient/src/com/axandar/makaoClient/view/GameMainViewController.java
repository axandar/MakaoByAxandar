package com.axandar.makaoClient.view;

import com.axandar.makaoClient.ClientProperties;
import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

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
    protected ImageView lastClickedIVToOrder;
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

        suitableCardsTypeToPut = properties.getSuitableCardsToOrder();
        List<Card> notAcceptedCards = properties.getNotAcceptedCards();
        puttedCards = properties.getPuttedCards();

        properties.setNotAcceptedCards(new ArrayList<>());
        properties.setSuitableCardsToOrder(new ArrayList<>());
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

    protected abstract void makeNotification(String title, String text);

    protected abstract void setCardOnTopTexture(Card card);

    protected abstract void populateListOfAnotherPlayers();
}
