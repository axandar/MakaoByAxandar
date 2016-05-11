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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 09.03.2016.
 */
public class GameController{

    // TODO: 29.04.2016 TODO
    //player wait turns --- not working
    //show to player last puttedCards and clear that array


    private final String TAG = "Client side";

    private volatile ClientProperties properties;
    private Player player;
    private List<Card> cardsToPut = new ArrayList<>();
    private List<Integer> suitableCardsTypeToPut = new ArrayList<>();
    private Card orderedCard = null;
    private Card ordered = null;

    @FXML private HBox cardsInHand;
    @FXML private ListView<String> playersList;
    @FXML private ImageView cardOnTop;
    @FXML private Button btnEndTurn;
    @FXML private Button btnSayMakao;
    @FXML private AnchorPane apOrderingCards;
    @FXML private ScrollPane spCardsTypeOrdering;
    @FXML private AnchorPane apColorCardsOrdering;
    @FXML private Button btnOrder;
    @FXML private Button btnCancel;
    @FXML private VBox imageViewsType;
    @FXML private ImageView ivCaro;
    @FXML private ImageView ivKier;
    @FXML private ImageView ivTrefl;
    @FXML private ImageView ivPik;

    // TODO: 26.04.2016 add option to say "Stop makao"
    public GameController(ClientProperties _clientProperties){
        properties = _clientProperties;
    }

    @FXML
    private void initialize(){
        Logger.logConsole(TAG, "Game started");

        Runnable updateGUI = this::updateGUI;
        Runnable endTurnBtnLogin = () ->{
            if(properties.isTurnStarted()){
                btnEndTurn.setDisable(false);
            }else btnEndTurn.setDisable(true);
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
                        Logger.logConsole(TAG, "Updated added to runLater()");
                        Platform.runLater(updateGUI);
                        properties.setUpdateGame(false);
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

        suitableCardsTypeToPut = properties.getSuitableCardsToOrder();
        properties.setSuitableCardsToOrder(new ArrayList<>());

        player = properties.getLocalPlayer();
        if(properties.getCardsToPut().size() > 0){
            properties.setCardsToPut(new ArrayList<>());
        }
        putCardsWasGood();
        Logger.logConsole(TAG, "Started adding cards");
        clearCards();
        for(Card card:player.getCardsInHand()){
            addCardToHandGUI(card);
        }

        setCardOnTopTexture(properties.getCardOnTop());

        List<Player> listOfRestPlayers = properties.getAdditionalPlayers();
        playersList.getItems().remove(0, playersList.getItems().size()-1);
        for(Player player:listOfRestPlayers){
            playersList.getItems().add(player.getPlayerName());
        }
        Logger.logConsole(TAG, "Update ended");
    }

    private void putCardsWasGood(){
        if(properties.isCardsRejected() && properties.getNotAcceptedCards().size() > 0){
            Logger.logConsole(TAG, "Some cards not accepted");
            Notifications.create().title("Wrong cards")
                    .text("Some of cards that you tried to send was incorrect").showWarning();
        }else if(properties.isCardsRejected()){
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
            properties.setCardsToPut(cardsToPut);
            if(cardsToPut.get(0).getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                    cardsToPut.get(0).getFunction().getFunctionID() == Function.ORDER_CARD){
                properties.setOrderedCard(orderedCard);
            }
            cardsToPut = new ArrayList<>();
        }

        Logger.logConsole(TAG, "ending turn");
        properties.setTurnStarted(false);
        properties.setTurnEnded(true);
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
            if(cardFromPlayer.getFunction().getFunctionID() == Function.CHANGE_COLOR ||
                    cardFromPlayer.getFunction().getFunctionID() == Function.ORDER_CARD){
                btnEndTurn.setDisable(true);

                btnCancel.setOnMouseClicked(event -> {
                    apOrderingCards.setVisible(false);
                    apColorCardsOrdering.setVisible(false);
                    spCardsTypeOrdering.setVisible(false);
                });

                btnOrder.setOnMouseClicked(event -> {
                    if(ordered != null){
                        orderedCard = ordered;
                        ordered = null;
                        apOrderingCards.setVisible(false);
                        apColorCardsOrdering.setVisible(false);
                        spCardsTypeOrdering.setVisible(false);
                        clickedImage.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                        cardsToPut.add(cardFromPlayer);

                        imageViewsType.getChildren().clear();

                        btnEndTurn.setDisable(false);
                    }
                });

                if(cardFromPlayer.getFunction().getFunctionID() == Function.ORDER_CARD){
                    apColorCardsOrdering.setVisible(false);
                    apOrderingCards.setVisible(true);
                    spCardsTypeOrdering.setVisible(true);

                    for(Integer suitableCard: suitableCardsTypeToPut) {
                        ImageView ivCard = new ImageView();
                        Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + suitableCard + ".png"));
                        ivCard.setImage(image);
                        ivCard.setFitWidth(96);
                        ivCard.setFitHeight(150);
                        ivCard.setId(suitableCard+"");

                        ivCard.setOnMouseClicked(event -> {
                            ImageView clickedImageOrder = (ImageView) event.getSource();

                            for(Integer suitableCardOnClicked: suitableCardsTypeToPut){
                                if(clickedImageOrder.getId().equals(suitableCardOnClicked+"")){
                                    if(ordered == null){
                                        clickedImageOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                                        ordered = new Card(0, suitableCardOnClicked, new Function(Function.NOTHING, 0));
                                    }else if(ordered.getIdType() == suitableCardOnClicked){
                                        clickedImage.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                                        ordered = new Card(0, suitableCardOnClicked, new Function(Function.NOTHING, 0));
                                    }// TODO: 11.05.2016 continue
                                    break;
                                }
                            }
                        });

                    }

                }else{
                    spCardsTypeOrdering.setVisible(false);
                    apOrderingCards.setVisible(true);
                    apColorCardsOrdering.setVisible(true);


                }

                //show available cards to order when trying to end turn
                //after player choose card to order set btnEndTurn.setDisable(false)
                // TODO: 26.04.2016 show window where player can choose ordered card
                // TODO: 26.04.2016 remember to add otpion for ordering "nothing"
            }else{
                clickedImage.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                cardsToPut.add(cardFromPlayer);
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
