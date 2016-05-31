package com.axandar.makaoDisplayPCJavaFx.Application.Controllers;

import com.axandar.makaoClient.ClientProperties;
import com.axandar.makaoClient.view.GameMainViewController;
import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Function;
import com.axandar.makaoCore.logic.Player;
import com.axandar.makaoCore.utils.Logger;
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
    @FXML private VBox imageViewsType;
    @FXML private ImageView ivCaro;
    @FXML private ImageView ivKier;
    @FXML private ImageView ivTrefl;
    @FXML private ImageView ivPik;
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

        if(orderedCard == null){
            ivOrderedCard.setVisible(false);
        }else if(orderedCard.getFunction().getFunctionID() == 1){
            image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + orderedCard.getIdType() + ".png"));
            ivOrderedCard.setImage(image);
            ivOrderedCard.setVisible(true);
        }else{
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
            handleClickOnCard(clickedImage.getId());
        });

        cardsInHand.getChildren().add(imageView);
        cardsInHandImageViews.add(imageView);
    }

    private void addSeparator(){
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
    public void endTurn(){
        handleEndTurn();
    }

    @FXML
    public void returnToMainView(){
        apOrderingCards.setVisible(false);
        apColorCardsOrdering.setVisible(false);
        spCardsTypeOrdering.setVisible(false);
        apLastPutCardsView.setVisible(false);
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
        // TODO: 31.05.2016 Not showing cards to choose order type
        int index = 0;
        List<ImageView> ivToAdd = new ArrayList<>();

        for(Integer suitableCard : suitableCardsTypeToPut){
            ImageView ivCard = new ImageView();
            Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + suitableCard + ".png"));
            ivCard.setImage(image);
            ivCard.setFitWidth(96);
            ivCard.setFitHeight(150);
            ivCard.setId(suitableCard + "");

            index++;
            ivCard.setOnMouseClicked(event -> {
                ImageView clickedImageOrder = (ImageView) event.getSource();
                clickedSelectOrderingTypeCard(clickedImageOrder);
            });
            ivToAdd.add(ivCard);

            if(index%3 == 0){
                HBox hb = new HBox();
                hb.setPrefHeight(150);
                // TODO: 11.05.2016 adding separators
                int x = 0;
                Separator separator = new Separator();
                separator.setPrefHeight(150);

                for(ImageView iv : ivToAdd){
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

    private void clickedSelectOrderingTypeCard(ImageView clickedImageOrder){
        for(Integer suitableCardOnClicked : suitableCardsTypeToPut){
            if(clickedImageOrder.getId().equals(suitableCardOnClicked + "")){
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
}