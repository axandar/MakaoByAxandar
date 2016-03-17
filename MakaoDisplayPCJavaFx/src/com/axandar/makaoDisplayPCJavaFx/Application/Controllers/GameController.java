package com.axandar.makaoDisplayPCJavaFx.Application.Controllers;

import com.axandar.makaoClient.Client;
import com.axandar.makaoClient.ClientProperties;
import com.axandar.makaoCore.logic.Card;
import com.axandar.makaoCore.logic.Deck;
import com.axandar.makaoCore.logic.Player;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 09.03.2016.
 */
public class GameController {

    private volatile ClientProperties clientProperties;
    private Deck deckInHand = new Deck();
    private Player player;
    private Card clickedCard;
    private Card orderedCard;

    private List<String> imageViewsIDs = new ArrayList<>();

    @FXML
    private FlowPane cardsInHand;

    @FXML
    private ListView<String> playersList;

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

    @FXML // TODO: 12.03.2016 przeniesc do miejsca w menu, gdzie startuje gra
    private void startGame(){
        clientProperties = new ClientProperties();
        Client client = new Client(clientProperties);
        Thread clientThread = new Thread(client);
        // TODO: 17.03.2016 Adding TableClient in another Thread or do all in Controller?
        Runnable updateGUI = () -> {
            player = clientProperties.getPlayer();
            // TODO: 12.03.2016 aktualizacja calego gui wedlug wytycznych z clientProperties BEZ PETLI
            //dodawanie nowych kart do ręki
            List<Card> cardsToAdd = getNewCards();
            cardsToAdd.forEach(this::addCardToHandGUI);

            //aktualizacja ilosc kart u innych graczy
            Player playerToUpdate = clientProperties.getPlayerToUpdate();
            for(Object objectFromList : playersList.getItems()){
                String playerStats = (String) objectFromList;
                if(playerStats.contains(playerToUpdate.getPlayerName())){
                    playerStats = playerToUpdate.getPlayerName() + " - " + playerToUpdate.getCardsInHand().size();
                    int indexToChange = playersList.getItems().indexOf(objectFromList);
                    playersList.getItems().remove(indexToChange);
                    playersList.getItems().add(indexToChange, playerStats);
                }
            }

        };

        Task taskToUpdateGUI = new Task(){
            @Override
            protected Object call() throws Exception{
                while(clientProperties.isClientRunning()){
                    if(clientProperties.isGameUpdate()){
                        Platform.runLater(updateGUI);
                        clientProperties.updatedGame();
                    }
                    Thread.sleep(2000);
                }
                return null;
            }
        };

        Thread updatingGUI = new Thread(taskToUpdateGUI);



        clientThread.setDaemon(true);//aby w razie problemu dokonczylo komunikacje z serwerem
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

    private void addCardToHandGUI(Card card){
        int cardColor = card.getIdColor();
        int cardType = card.getIdType();
        String cardFileName = cardType + "-" + cardColor;

        ImageView imageView = new ImageView();
        Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + cardFileName + ".png"));
        imageView.setImage(image);
        imageView.setFitWidth(96);
        imageView.setFitHeight(150);
        imageView.setId(cardFileName);
        imageViewsIDs.add(cardFileName);

        imageView.setOnMouseClicked(event -> {
            ImageView clickedImage = (ImageView) event.getSource();

            for(Card cardFromPlayer : player.getCardsInHand()){
                String cardName = cardFromPlayer.getIdType() + "-" + cardFromPlayer.getIdColor();
                if(clickedImage.getId().equals(cardName)){
                    clickedCard = cardFromPlayer;
                    break;
                }
            }
        });

        cardsInHand.getChildren().add(imageView);

        if(deckInHand.deckLength() > 1){
            Separator separator = new Separator();
            separator.setPrefHeight(150);

            cardsInHand.getChildren().add(separator);
        }
    }


    @FXML
    private void initialize() {
        /**clientMain = new Client();
        Label label = new Label("");
        cardsInHand.getChildren().add(label);
        Thread t = new Thread(clientMain);
        t.start();
        Task task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                while(clientMain.isClientRunning()){

                    if(clientMain.isGameUpdate()){//Sprawdza czy byla aktualizacja klienta
                        Platform.runLater(new Runnable() {
                            public void run() {
                                label.setText(clientMain.isGameUpdate()+"");
                                System.out.println(clientMain.isGameUpdate());
                                //aktualizacja interfejsu
                            }
                        });
                    }
                    Thread.sleep(2000);
                }
                return null;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();**/
    }


   /** private void putCardInHand(Card card){
        int cardColor = card.getIdColor();
        int cardType = card.getIdType();
        String cardFileName = cardType + "-" + cardColor;

        ImageView imageView = new ImageView();
        Image image = new Image(this.getClass().getResourceAsStream("/TaliaKart/" + cardFileName + ".png"));
        imageView.setImage(image);
        imageView.setFitWidth(96);
        imageView.setFitHeight(150);
        imageView.setId(cardFileName);
        imageViewsIDs.add(cardFileName);

        imageView.setOnMouseClicked(event -> {
            ImageView clickedImage = (ImageView) event.getSource();
            System.out.println(cardsInHand.getChildren().indexOf(clickedImage));
        });

        cardsInHand.getChildren().add(imageView);

        if(deckInHand.deckLength() > 1){
            Separator separator = new Separator();
            separator.setPrefHeight(150);

            cardsInHand.getChildren().add(separator);
        }
    }**/

}
