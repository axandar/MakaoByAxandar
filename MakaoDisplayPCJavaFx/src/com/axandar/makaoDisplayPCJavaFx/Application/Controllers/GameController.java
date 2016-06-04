package com.axandar.makaoDisplayPCJavaFx.Application.Controllers;

import com.axandar.makaoClient.ClientProperties;
import com.axandar.makaoClient.view.GameMainViewController;
import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.controlsfx.control.Notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 09.03.2016.
 */


public class GameController extends GameMainViewController{

    @FXML private HBox cardsInHand;
    @FXML private ImageView cardOnTop;
    @FXML private ListView<String> playersList;
    @FXML private Button btnEndTurn;
    @FXML private Button btnSayMakao;
    @FXML private Button btnSayStopMakao;
    @FXML private ImageView ivOrderedCard;
    @FXML private AnchorPane apLastPutCardsView;
    @FXML private ScrollPane spLastPutCards;
    @FXML private Button btnExitView;
    @FXML private AnchorPane apOrderingCards;
    @FXML private ScrollPane spCardsTypeOrdering;
    @FXML private AnchorPane apColorCardsOrdering;
    @FXML private Button btnOrder;
    @FXML private Button btnCancel;
    @FXML private GridPane imageViewsType;
    @FXML private ImageView ivCaro;
    @FXML private ImageView ivKier;
    @FXML private ImageView ivTrefl;
    @FXML private ImageView ivPik;
    @FXML private VBox imageViewsLastPlacedCards;
    private ImageView lastClickedIVToOrder;
    
    private List<ImageView> cardsInHandImageViews = new ArrayList<>();

    public GameController(ClientProperties _clientProperties){
        properties = _clientProperties;
    }

    @FXML
    private void initialize(){
        initializeController();
    }

    @Override
    protected void setCardOnTopTexture(Card card){
        Logger.logConsole(TAG, "Received card on top");
        int cardColor = card.getIdColor();
        int cardType = card.getIdType();
        String cardFileName = cardType + "-" + cardColor;
        Logger.logConsole(TAG, "File name of card texture to add on top: " + cardFileName);

        Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + cardFileName + ".png"));
        cardOnTop.setImage(image);
    }

    @Override
    protected void setGUIOnTurnActive(){
        btnEndTurn.setDisable(false);
        btnSayMakao.setDisable(false);
    }

    @Override
    protected void setGUIOnTurnInactive(){
        btnEndTurn.setDisable(true);
        btnSayMakao.setDisable(true);
    }

    @Override
    protected void setOrderedCardImage(){
        Card orderedCard = properties.getOrderedCard();
        Image image;

        if(orderedCard != null && orderedCard.getFunction().getFunctionValue() == 0){
            ivOrderedCard.setVisible(false);
        }else if(orderedCard != null && orderedCard.getFunction().getFunctionValue() == 1){
            image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + orderedCard.getIdType() + ".png"));
            ivOrderedCard.setImage(image);
            ivOrderedCard.setVisible(true);
        }else if(orderedCard != null){
            image = new Image(this.getClass().getResourceAsStream("/TaliaKart/c" + orderedCard.getIdColor() + ".png"));
            ivOrderedCard.setImage(image);
            ivOrderedCard.setVisible(true);
        }
    }

    @Override
    protected void clearHandFromCards(){
        cardsInHand.getChildren().clear();
        cardsInHandImageViews.clear();
    }

    @Override
    protected void addCardToHandGUI(Card card){
        int cardColor = card.getIdColor();
        int cardType = card.getIdType();
        String cardFileName = cardType + "-" + cardColor;

        if(cardsInHand.getChildren().size() > 0){
            addSeparatorToCardsInHand();
        }

        ImageView imageView = new ImageView();
        Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + cardFileName + ".png"));
        imageView.setImage(image);
        imageView.setFitWidth(96);
        imageView.setFitHeight(150);
        imageView.setId(cardFileName);

        imageView.setOnMouseClicked(event -> {
            ImageView clickedImage = (ImageView) event.getSource();
            handleClickOnCard(clickedImage.getId());
        });

        cardsInHand.getChildren().add(imageView);
        cardsInHandImageViews.add(imageView);
    }

    private void addSeparatorToCardsInHand(){
        Separator separator = new Separator();
        separator.setPrefHeight(150);
        separator.getStyleClass().add("betweenCardsSeparator");

        cardsInHand.getChildren().add(separator);
    }

    @Override
    protected void cardClickedSecondTime(String cardViewContainerID){
        getImageViewByID(cardViewContainerID)
                .setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0), 0, 0, 0, 0)");
        btnEndTurn.setDisable(false);
    }

    @Override
    protected void clickedOrderCardFirstTime(String cardViewContainerID, Card cardFromPlayer){
        //show window for choose order card
        btnEndTurn.setDisable(true);

        if(cardFromPlayer.getFunction().getFunctionID() == Function.ORDER_CARD){
            apColorCardsOrdering.setVisible(false);
            apOrderingCards.setVisible(true);
            spCardsTypeOrdering.setVisible(true);

            handleClickedOrderingType();
        }else{
            spCardsTypeOrdering.setVisible(false);
            apOrderingCards.setVisible(true);
            apColorCardsOrdering.setVisible(true);

            handleClickedOrderingColor();
            // TODO: 11.05.2016 ordering color
        }
    }

    @Override
    protected void clickedNormalCardFirstTime(String cardViewContainerID){
        getImageViewByID(cardViewContainerID)
                .setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
    }

    private ImageView getImageViewByID(String imageViewID){
        for(ImageView iv:cardsInHandImageViews){
            if(iv.getId().equals(imageViewID)){
                return iv;
            }
        }
        Logger.logConsole(TAG, "Error in finding proper ImageView");
        return new ImageView();
    }

    @Override
    protected void makeNotification(String title, String text){
        Notifications.create().title(title).text(text).showWarning();
    }

    @Override
    protected void populateListOfAnotherPlayers(){
        List<Player> listOfRestPlayers = properties.getAdditionalPlayers();
        playersList.getItems().remove(0, playersList.getItems().size() - 1);
        for(Player player : listOfRestPlayers){
            playersList.getItems().add(player.getPlayerName());
        }
    }

    @FXML
    public void endTurn(){
        handleEndTurn();
    }

    @FXML
    public void returnToMainView(){
        clearHandFromCards();
        for(Card card : player.getCardsInHand()){
            addCardToHandGUI(card);
        }

        if(orderedCard == null){
            cardsToPut.remove(cardsToPut.size()-1);
        }

        imageViewsType.getChildren().clear();

        apOrderingCards.setVisible(false);
        apColorCardsOrdering.setVisible(false);
        spCardsTypeOrdering.setVisible(false);
    }

    @FXML
    public void orderCard(){
        if(ordered != null){
            orderedCard = ordered;
            ordered = null;

            returnToMainView();
            lastClickedIVToOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
            imageViewsType.getChildren().clear();
            btnEndTurn.setDisable(false);
        }
    }

    private void handleClickedOrderingType(){
        orderedCard = null;
        Logger.logConsole(TAG, "Received " + suitableCardsTypeToPut.size() + " suitable cards");
        setTableWithCardsTypes();
    }

    private void setTableWithCardsTypes(){
        int cardInViewHeight = 225;
        int cardInViewWidth = 144;

        imageViewsType.getColumnConstraints().add(new ColumnConstraints(cardInViewWidth/4));
        imageViewsType.getColumnConstraints().add(new ColumnConstraints(cardInViewWidth));
        imageViewsType.getColumnConstraints().add(new ColumnConstraints(cardInViewWidth/4));
        imageViewsType.getColumnConstraints().add(new ColumnConstraints(cardInViewWidth));
        imageViewsType.getColumnConstraints().add(new ColumnConstraints(cardInViewWidth/4));
        imageViewsType.getColumnConstraints().add(new ColumnConstraints(cardInViewWidth));
        imageViewsType.getColumnConstraints().add(new ColumnConstraints(cardInViewWidth/4));
        imageViewsType.getRowConstraints().add(new RowConstraints(cardInViewWidth/4));
        imageViewsType.getRowConstraints().add(new RowConstraints(cardInViewHeight));

        int row = 1;
        int column = 1;
        for(Integer suitableCard : suitableCardsTypeToPut){
            ImageView ivCard = new ImageView();
            Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + suitableCard + ".png"));
            ivCard.setImage(image);
            ivCard.setFitWidth(cardInViewWidth);
            ivCard.setFitHeight(cardInViewHeight);
            ivCard.setId(suitableCard + "");

            ivCard.setOnMouseClicked(event -> {
                ImageView clickedImageOrder = (ImageView) event.getSource();
                clickedSelectOrderingTypeCard(clickedImageOrder);
            });

            if(column <= 5){
                imageViewsType.add(ivCard, column, row);
                column+=2;
            }else{
                column = 1;
                row+=2;
                imageViewsType.getRowConstraints().add(new RowConstraints(cardInViewWidth/4));
                imageViewsType.getRowConstraints().add(new RowConstraints(cardInViewHeight));
                imageViewsType.add(ivCard, column, row);
                column+=2;
            }
        }
        imageViewsType.getRowConstraints().add(new RowConstraints(cardInViewWidth/4));
    }

    private void clickedSelectOrderingTypeCard(ImageView clickedImageOrder){
        for(Integer suitableCardOnClicked : suitableCardsTypeToPut){
            if(clickedImageOrder.getId().equals(suitableCardOnClicked + "")){
                Logger.logConsole(TAG, "Clicked order card type: " + suitableCardOnClicked);
                if(ordered != null){
                    clickedImageOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                    ordered = new Card(0, suitableCardOnClicked, new Function(Function.ORDERED, 1));
                    lastClickedIVToOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 0, 0, 0, 0)");
                    lastClickedIVToOrder = clickedImageOrder;
                }else{
                    clickedImageOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
                    ordered = new Card(0, suitableCardOnClicked, new Function(Function.ORDERED, 1));
                    lastClickedIVToOrder = clickedImageOrder;
                }
                break;
            }
        }
    }

    private void handleClickedOrderingColor(){
        Image ivCaroImage = new Image(this.getClass().getResourceAsStream("/TaliaKart/c0.png"));
        ivCaro.setImage(ivCaroImage);

        Image ivKierImage = new Image(this.getClass().getResourceAsStream("/TaliaKart/c2.png"));
        ivKier.setImage(ivKierImage);

        Image ivTreflImage = new Image(this.getClass().getResourceAsStream("/TaliaKart/c1.png"));
        ivTrefl.setImage(ivTreflImage);

        Image ivPikImage = new Image(this.getClass().getResourceAsStream("/TaliaKart/c3.png"));
        ivPik.setImage(ivPikImage);
    }

    @FXML
    public void clickedColorCaro(){
        clickedColorToOrder(ivCaro, Card.COLOR_KARO);
    }

    @FXML
    public void clickedColorKier(){
        clickedColorToOrder(ivKier, Card.COLOR_KIER);
    }

    @FXML
    public void clickedColorTrefl(){
        clickedColorToOrder(ivTrefl, Card.COLOR_TREFL);
    }

    @FXML
    public void clickedColorPik(){
        clickedColorToOrder(ivPik, Card.COLOR_PIK);
    }

    private void clickedColorToOrder(ImageView ivColor, int colorID){
        if(ordered != null){
            ivColor.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
            ordered = new Card(colorID, 0, new Function(Function.ORDERED, 2));
            lastClickedIVToOrder.setStyle("-fx-effect: dropshadow(three-pass-box, red, 0, 0, 0, 0)");
            lastClickedIVToOrder = ivColor;
        }else{
            ivColor.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 0, 0, 0)");
            ordered = new Card(colorID, 0, new Function(Function.ORDERED, 2));
            lastClickedIVToOrder = ivColor;
        }
    }

    @FXML
    public void sayMakao(){
        handleSayMakao();
    }

    protected void setSayMakao(boolean isMakao){
        if(isMakao){
            btnSayMakao.setText("Say not makao");
        }else {
            btnSayMakao.setText("Say makao");
        }
    }

    @FXML
    public void sayStopMakao(){
        handleSayStopMakao(null);
    }

    @FXML
    public void showLastPlacedCards(){
        addLastPlacedCardsToView();

        apLastPutCardsView.setVisible(true);
    }

    private void addLastPlacedCardsToView(){
        int cardInViewHeight = 150;
        int cardInViewWidth = 96;

        for(List<Card> cardsList:cardsPlacedByAnotherPLayers){
            ScrollPane sPane = new ScrollPane();
            sPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            sPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            sPane.setHmax(cardInViewHeight + 10);
            sPane.setHmin(cardInViewHeight + 10);

            HBox hBox = new HBox();
            hBox.setMaxHeight(cardInViewHeight);
            hBox.setMinHeight(cardInViewHeight);

            for(Card card:cardsList){
                Logger.logConsole(TAG, "Adding card to last placed cards view");
                int cardColor = card.getIdColor();
                int cardType = card.getIdType();
                String cardFileName = cardType + "-" + cardColor;

                if(hBox.getChildren().size() > 0){
                    addSeparatorToLastPlacedCards(cardInViewHeight);
                }

                ImageView imageView = new ImageView();
                Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + cardFileName + ".png"));
                imageView.setImage(image);
                imageView.setFitWidth(cardInViewWidth);
                imageView.setFitHeight(cardInViewHeight);

                hBox.getChildren().add(imageView);
            }

            sPane.setContent(hBox);
            imageViewsLastPlacedCards.getChildren().add(sPane);
            imageViewsLastPlacedCards.getChildren().add(new Label());
        }
    }

    private void addSeparatorToLastPlacedCards(int height){
        Separator separator = new Separator();
        separator.setPrefHeight(height);
        imageViewsLastPlacedCards.getChildren().add(separator);
    }

    @FXML
    public void closePlacedCardsView(){
        apLastPutCardsView.setVisible(false);
        imageViewsLastPlacedCards.getChildren().clear();
    }
}