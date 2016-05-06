package com.axandar.makaoDisplayPCJavaFx.Application.Controllers;

import com.axandar.makaoClient.ClientProperties;
import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.Notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 09.03.2016.
 */
public class GameController{

    // TODO: 29.04.2016 TODO
    //player wait turns --- not working


    private final String TAG = "Client side";

    private volatile ClientProperties clientProperties;
    private Player player;
    private List<Card> cardsToPut = new ArrayList<>();
    private Card orderedCard = null;

    @FXML private HBox cardsInHand;
    @FXML private ListView<String> playersList;
    @FXML private ImageView cardOnTop;
    @FXML private Button btnEndTurn;
    @FXML private Button btnSayMakao;

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

    // TODO: 26.04.2016 add option to say "Stop makao"
    public GameController(ClientProperties _clientProperties){
        clientProperties = _clientProperties;
    }

    @FXML
    private void initialize(){
        Logger.logConsole(TAG, "Game started");

        Runnable updateGUI = this::updateGUI;
        Runnable endTurnBtnLogin = () ->{
            if(clientProperties.isTurnStarted()){
                btnEndTurn.setDisable(false);
            }else btnEndTurn.setDisable(true);
        };

        Task taskToUpdateGUI = new Task(){
            @Override
            protected Object call() throws Exception{
                while(!clientProperties.isClientRunning()){
                    Thread.sleep(1000);
                    //waiting for client initialize
                }
                while(clientProperties.isClientRunning()){
                    Platform.runLater(endTurnBtnLogin);
                    if(clientProperties.isUpdateGame()){
                        Logger.logConsole(TAG, "Updated added to runLater()");
                        Platform.runLater(updateGUI);
                        clientProperties.setUpdateGame(false);
                    }
                    Platform.runLater(endTurnBtnLogin);
                    Thread.sleep(2000);
                }
                return null;
            }
        };
        Thread updatingGUI = new Thread(taskToUpdateGUI);
        updatingGUI.start();
    }

    private void updateGUI(){
        Logger.logConsole(TAG, "Start updating GUI");

        player = clientProperties.getLocalPlayer();
        if(clientProperties.getCardsToPut().size() > 0){
            clientProperties.setCardsToPut(new ArrayList<>());
        }
        putCardsWasGood();
        Logger.logConsole(TAG, "Started adding cards");
        clearCards();
        for(Card card:player.getCardsInHand()){
            addCardToHandGUI(card);
        }

        setCardOnTopTexture(clientProperties.getCardOnTop());

        List<Player> listOfRestPlayers = clientProperties.getAdditionalPlayers();
        playersList.getItems().remove(0, playersList.getItems().size()-1);
        for(Player player:listOfRestPlayers){
            playersList.getItems().add(player.getPlayerName());
        }
        Logger.logConsole(TAG, "Update ended");
    }

    private void putCardsWasGood(){
        if(clientProperties.isCardsRejected() && clientProperties.getNotAcceptedCards().size() > 0){
            Logger.logConsole(TAG, "Some cards not accepted");
            Notifications.create().title("Wrong cards")
                    .text("Some of cards that you tried to send was incorrect").showWarning();
        }else if(clientProperties.isCardsRejected()){
            Logger.logConsole(TAG, "Cards was not equals");
            Notifications.create().title("Wrong cards")
                    .text("Cards which you have tried to send was not equal function").showWarning();
        }
    }

    private void clearCards(){
        cardsInHand.getChildren().clear();
    }

    @FXML
    public void endTurn(){
        if(cardsToPut.size() > 0){
            Logger.logConsole(TAG, "Requested sending cards");
            clientProperties.setCardsToPut(cardsToPut);
            if(cardsToPut.get(0).getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                    cardsToPut.get(0).getFunction().getFunctionID() == Function.ORDER_CARD){
                clientProperties.setOrderedCard(orderedCard);
            }
            cardsToPut = new ArrayList<>();
        }

        Logger.logConsole(TAG, "ending turn");
        clientProperties.setTurnStarted(false);
        clientProperties.setTurnEnded(true);
    }

    private void addCardToHandGUI(Card card){
        int cardColor = card.getIdColor();
        int cardType = card.getIdType();
        String cardFileName = cardType + "-" + cardColor;

        if(cardsInHand.getChildren().size() > 0){
            addSeparator();
        }

        ImageView imageView = new ImageView();
        Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + cardFileName + ".png"));
        imageView.setImage(image);
        imageView.setFitWidth(96);
        imageView.setFitHeight(150);
        imageView.setId(cardFileName);

        imageView.setOnMouseClicked(event -> {
            ImageView clickedImage = (ImageView) event.getSource();
            handleClickOnCard(clickedImage);
        });

        cardsInHand.getChildren().add(imageView);
    }

    private void addSeparator(){
        Separator separator = new Separator();
        separator.setPrefHeight(150);
        separator.getStyleClass().add("betweenCardsSeparator");

        cardsInHand.getChildren().add(separator);
    }

    private void handleClickOnCard(ImageView clickedImage){
        for(Card cardFromPlayer : player.getCardsInHand()){
            String cardName = cardFromPlayer.getIdType() + "-" + cardFromPlayer.getIdColor();
            if(clickedImage.getId().equals(cardName)){
                changeClickedCardState(clickedImage, cardFromPlayer);
                Logger.logConsole(TAG, "Clicked card: " + cardName);
                break;
            }
        }
    }

    private void changeClickedCardState(ImageView clickedImage, Card cardFromPlayer){
        if(cardsToPut.contains(cardFromPlayer)){
            clickedImage.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0), 0, 0, 0, 0)");
            cardsToPut.remove(cardFromPlayer);
            btnEndTurn.setDisable(false);
        }else{
            clickedImage.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
            cardsToPut.add(cardFromPlayer);
            if(cardFromPlayer.getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                    cardFromPlayer.getFunction().getFunctionID() == Function.ORDER_CARD){
                btnEndTurn.setDisable(true);

                //show available cards to order when trying to end turn

                //after player choose card to order set btnEndTurn.setDisable(false)
                // TODO: 26.04.2016 show window where player can choose ordered card
                // TODO: 26.04.2016 remember to add otpion for ordering "nothing"
            }
        }
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
