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
    //when only two players and one is waiting the waiting one is not showing updated card on top

    //when putting functional king, wrong player is chose next

    //order card type for all players and color for only next

    ///////show ordered card ???and from which player???

    //when player clicked on card on top, displaying new vertical scroll pane with last putted cards

    //complete color ordering

    //add option to say makao before turn ending
    //add option to say stop makao while in turn and after ending it


    private final String TAG = "Client side";

    private volatile ClientProperties properties;
    private Player player;
    private List<Card> cardsToPut = new ArrayList<>();
    private List<Integer> suitableCardsTypeToPut = new ArrayList<>();
    private Card orderedCard = null;
    private Card ordered = null;
    private ImageView lastClickedIVToOrder;
    private List<Card> puttedCards = new ArrayList<>();

    @FXML private HBox cardsInHand;
    @FXML private ListView<String> playersList;
    @FXML private ImageView cardOnTop;
    @FXML private Button btnEndTurn;
    @FXML private Button btnSayMakao;
    //
    @FXML private Button btnSayStopMakao;
    @FXML private ImageView ivOrderedCard;
    @FXML private AnchorPane apLastPutCardsView;
    @FXML private ScrollPane spLastPutCards;
    @FXML private Button btnExitView;
    //
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
                        Logger.logConsole(TAG, "Update added to runLater()");
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
        List<Card> notAcceptedCards = properties.getNotAcceptedCards();
        puttedCards = properties.getPuttedCards();//show somewhere in GUI

        properties.setNotAcceptedCards(new ArrayList<>());
        properties.setSuitableCardsToOrder(new ArrayList<>());
        properties.setPuttedCards(new ArrayList<>());

        setOrderedCardImage();

        player = properties.getLocalPlayer();

        if(properties.getCardsToPut().size() > 0 && properties.getNotAcceptedCards().size() == 0){
            properties.setCardsToPut(new ArrayList<>());
        }else if(notAcceptedCards.size() > 0){
            for(Card card: notAcceptedCards){
                properties.getLocalPlayer().removeCardFromHand(card);
            }
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

    private void setOrderedCardImage(){
        Card orderedCard = properties.getOrderedCard();
        Image image;

        if(orderedCard == null){
            ivOrderedCard.setVisible(false);
        }else if(orderedCard.getFunction().getFunctionID() == 1){
            image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + orderedCard.getIdType() + ".png"));
            ivOrderedCard.setImage(image);
            ivOrderedCard.setVisible(true);
            // TODO: 16.05.2016 check name correction
        }else{
            image = new Image(this.getClass().getResourceAsStream("/TaliaKart/c" + orderedCard.getIdColor() + ".png"));
            ivOrderedCard.setImage(image);
            ivOrderedCard.setVisible(true);
        }
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

        properties.setTurnStarted(false);
        properties.setTurnEnded(true);
        Logger.logConsole(TAG, "Ended turn");
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
                clickedOrderCard(clickedImage, cardFromPlayer);
            }else{
                clickedImage.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                cardsToPut.add(cardFromPlayer);
            }
        }
    }

    private void clickedOrderCard(ImageView clickedImage, Card cardFromPlayer){
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

            handleClickedOrderingType();
        }else{
            spCardsTypeOrdering.setVisible(false);
            apOrderingCards.setVisible(true);
            apColorCardsOrdering.setVisible(true);

            // TODO: 11.05.2016 ordering color
        }

    }

    private void handleClickedOrderingType(){
        int index = 0;
        List<ImageView> ivToAdd = new ArrayList<>();

        for(Integer suitableCard: suitableCardsTypeToPut) {
            ImageView ivCard = new ImageView();
            Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + suitableCard + ".png"));
            ivCard.setImage(image);
            ivCard.setFitWidth(96);
            ivCard.setFitHeight(150);
            ivCard.setId(suitableCard+"");

            index++;
            ivCard.setOnMouseClicked(event -> {
                ImageView clickedImageOrder = (ImageView) event.getSource();
                selectedOrderingType(clickedImageOrder);
            });
            ivToAdd.add(ivCard);

            if(index%3 == 0){
                HBox hb = new HBox();
                hb.setPrefHeight(150);
                // TODO: 11.05.2016 adding separators
                int x = 0;
                Separator separator = new Separator();
                separator.setPrefHeight(150);

                for(ImageView iv:ivToAdd){
                    hb.getChildren().add(iv);
                    if(x < 3){
                        hb.getChildren().add(separator);
                        x++;
                    }
                }
                imageViewsType.getChildren().add(hb);
            }
        }
    }

    private void selectedOrderingType(ImageView clickedImageOrder){
        for(Integer suitableCardOnClicked: suitableCardsTypeToPut){
            if(clickedImageOrder.getId().equals(suitableCardOnClicked+"")){
                Logger.logConsole(TAG, "Clicked order card type: " + suitableCardOnClicked);
                if(ordered != null){
                    clickedImageOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                    ordered = new Card(0, suitableCardOnClicked, new Function(Function.NOTHING, 1));
                    lastClickedIVToOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 0, 0, 0, 0)");
                    lastClickedIVToOrder = clickedImageOrder;
                }else{
                    clickedImageOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                    ordered = new Card(0, suitableCardOnClicked, new Function(Function.NOTHING, 1));
                    //cards ordered only type have function value = 1
                    lastClickedIVToOrder = clickedImageOrder;
                }
                break;
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
