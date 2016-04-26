package com.axandar.makaoDisplayPCJavaFx.Application.Controllers;

import com.axandar.makaoClient.Client;
import com.axandar.makaoClient.ClientProperties;
import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Deck;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 09.03.2016.
 */
public class GameController {

    private final String TAG = "Client side";

    private volatile ClientProperties clientProperties;
    private Deck deckInHand = new Deck();
    private Player player;
    private List<Card> cardsToPut = new ArrayList<>();
    private Card orderedCard = null;

    private List<String> imageViewsIDs = new ArrayList<>();

    @FXML private HBox cardsInHand;
    @FXML private ListView<String> playersList;
    @FXML private ImageView cardOnTop;

    /**Stage dialogStage = new Stage();
     dialogStage.setTitle(rb.getString("AddingNewSupplierOrder"));
     dialogStage.initModality(Modality.WINDOW_MODAL);
     dialogStage.initOwner(getPrimaryStage());
     Scene scene = new Scene(page);
     dialogStage.setScene(scene);

     ShowAddingNewSupplierOrderController controller = loader.getController();
     controller.setDialogStage(dialogStage);
     controller.setData();

     dialogStage.showAndWait();**/

    @FXML
    public void startGame(){
        Logger.logConsole(TAG, "Game started");

        clientProperties = new ClientProperties();
        clientProperties.setIp("0.0.0.0");
        clientProperties.setPort(5000);
        clientProperties.setNickname("Axandar2");

        Client client = new Client(clientProperties);
        Thread clientThread = new Thread(client);

        Runnable updateGUI = () -> {
            Logger.logConsole(TAG, "Updated GUI");

            player = clientProperties.getLocalPlayer();

            if(clientProperties.getCardsToPut().size() > 0){
                Logger.logConsole(TAG, "Cards accepted and removing");
                clientProperties.setCardsToPut(new ArrayList<>());
                removeCardsFromHand();
            }

            if(clientProperties.isCardsRejected()){
                Logger.logConsole(TAG, "Some ards not accepted");
                // TODO: 26.04.2016 for each notAcceptedCard show alert about failure
            }

            Logger.logConsole(TAG, "Number of cards in hand on view: " + cardsInHand.getChildren().size()/2);
            Logger.logConsole(TAG, "Number of cards in hand in player object: " + player.getCardsInHand().size());
            if(cardsInHand.getChildren().size() == 0){
                Logger.logConsole(TAG, "Started adding cards");
                for(Card card:player.getCardsInHand()){
                    deckInHand.addCardToDeck(card);
                    addCardToHandGUI(card);
                }
            }else{
                List<Card> cardsToAdd = getNewCards();
                for(Card card:cardsToAdd){
                    deckInHand.addCardToDeck(card);
                    addCardToHandGUI(card);
                }
            }

            setCardOnTopTexture(clientProperties.getCardOnTop());

            List<Player> listOfRestPlayers = clientProperties.getAditionalPlayers();
            playersList.getItems().remove(0, playersList.getItems().size()-1);
            for(Player player:listOfRestPlayers){
                playersList.getItems().add(player.getPlayerName());
            }

        };

        Task taskToUpdateGUI = new Task(){
            @Override
            protected Object call() throws Exception{
                while(clientProperties.isClientRunning()){
                    if(clientProperties.isUpdateGame()){
                        Platform.runLater(updateGUI);
                        clientProperties.setUpdateGame(false);
                    }
                    Thread.sleep(2000);
                }
                return null;
            }
        };

        Thread updatingGUI = new Thread(taskToUpdateGUI);

        clientThread.start();
        updatingGUI.start();
    }

    private List<Card> getNewCards(){
        List<Card> newCards = new ArrayList<>();
        List<Card> cardsFromPlayer = player.getCardsInHand();

        for(Card card : cardsFromPlayer){
            String cardName = card.getIdType() + "-" + card.getIdColor();
            if(!imageViewsIDs.contains(cardName)){
                newCards.add(card);
            }
        }

        return newCards;
    }

    /**if(clickedCard != null){
     String cardName = clickedCard.getIdType() + "-" + clickedCard.getIdColor();
     Logger.logConsole(TAG, "Send card: " + cardName);
     clientProperties.setCardToPut(clickedCard);
     if(clickedCard.getFunction().getFunctionID() == Function.ORDER_CARD
     || clickedCard.getFunction().getFunctionID() == Function.CHANGE_COLOR){
     if(orderedCard != null){
     clientProperties.setOrderedCard(orderedCard);
     }else clientProperties.setOrderedCard(new Card(1, 1, new Function(4, 0)));
     }
     }**/

    @FXML
    public void sendCardToServer(){
        clientProperties.setCardsToPut(cardsToPut);
        if(cardsToPut.get(0).getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                cardsToPut.get(0).getFunction().getFunctionID() == Function.ORDER_CARD){
            clientProperties.setOrderedCard(orderedCard); 
        }
        clientProperties.setTurnEnded(true);
    }

    private void removeCardsFromHand(){
        //remove only that cards which are not in rejected but are in putted


        /**deckInHand.removeCardFromDeck(card);
        String cardFileName = card.getIdType() + "-" + card.getIdColor();
        for(Node node:cardsInHand.getChildren()){
            if(node.getId().equals(cardFileName)){
                cardsInHand.getChildren().remove(node);
                // TODO: 03.04.2016 not working removing from gui
                break;
            }
        }**/
    }

    @FXML
    public void endTurn(){
        clientProperties.setTurnEnded(true);
        /**deckInHand.addCardToDeck(new Card(1, 1, new Function(6, 0)));
        addCardToHandGUI(new Card(1, 1, new Function(6, 0)));
        deckInHand.addCardToDeck(new Card(1, 1, new Function(6, 0)));
        addCardToHandGUI(new Card(2, 2, new Function(6, 0)));
        deckInHand.addCardToDeck(new Card(1, 1, new Function(6, 0)));
        addCardToHandGUI(new Card(3, 3, new Function(6, 0)));**/
    }

    private void addCardToHandGUI(Card card){
        int cardColor = card.getIdColor();
        int cardType = card.getIdType();
        String cardFileName = cardType + "-" + cardColor;

        if(deckInHand.deckLength() > 1){
            Separator separator = new Separator();
            separator.setId(cardFileName);
            separator.setPrefHeight(150);
            separator.getStyleClass().add("betweenCardsSeparator");

            cardsInHand.getChildren().add(separator);
        }

        Logger.logConsole(TAG, "File name of card to add in hand: " + cardFileName);
        ImageView imageView = new ImageView();
        Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + cardFileName + ".png"));
        imageView.setImage(image);
        imageView.setFitWidth(96);
        imageView.setFitHeight(150);
        imageView.setId(cardFileName);
        imageViewsIDs.add(cardFileName);

        imageView.setOnMouseClicked(event -> {
            ImageView clickedImage = (ImageView) event.getSource();
            // TODO: 26.04.2016 add border when clicked, and remove it when clicked second time
            for(Card cardFromPlayer : player.getCardsInHand()){
                String cardName = cardFromPlayer.getIdType() + "-" + cardFromPlayer.getIdColor();
                if(clickedImage.getId().equals(cardName)){

                    if(cardsToPut.contains(cardFromPlayer)){
                        cardsToPut.remove(cardFromPlayer);
                    }else{
                        cardsToPut.add(cardFromPlayer);
                        if(cardFromPlayer.getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                                cardFromPlayer.getFunction().getFunctionID() == Function.ORDER_CARD){
                            // TODO: 26.04.2016 show window where player can choose ordered card
                            // TODO: 26.04.2016 remember to add otpion for ordering "nothing"
                        }
                    }

                    Logger.logConsole(TAG, "Clicked card: " + cardName);
                    break;
                }
            }
        });

        cardsInHand.getChildren().add(imageView);
    }

    private void setCardOnTopTexture(Card card){
        Logger.logConsole(TAG, "Received card on top");
        int cardColor = card.getIdColor();
        int cardType = card.getIdType();
        String cardFileName = cardType + "-" + cardColor;
        Logger.logConsole(TAG, "File name of card texture to add on top: " + cardFileName);

        Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + cardFileName + ".png"));
        cardOnTop.setImage(image);
    }

}
