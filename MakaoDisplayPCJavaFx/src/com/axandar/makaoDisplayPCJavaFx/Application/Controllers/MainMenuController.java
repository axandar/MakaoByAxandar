package com.axandar.makaoDisplayPCJavaFx.Application.Controllers;

import com.axandar.makaoCore.utils.Logger;
import com.axandar.makaoDisplayPCJavaFx.Application.LoadApplication;
import com.axandar.makaoDisplayPCJavaFx.Main;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Axandar on 09.03.2016.
 */
public class MainMenuController extends Application{

    @FXML
    private AnchorPane apMainMenu;
    @FXML
    private AnchorPane apAnotherOptions;
    @FXML
    private AnchorPane apSingleplayer = new AnchorPane();
    @FXML
    private AnchorPane apMultiplayer;
    @FXML
    private TextField tfNickname;
    @FXML
    private TextField tfIP;
    @FXML
    private TextField tfPort;
    @FXML
    private Button btnConnect;
    @FXML
    private AnchorPane apHighscore = new AnchorPane();
    @FXML
    private AnchorPane apSettings = new AnchorPane();
    @FXML
    private AnchorPane apAuthor = new AnchorPane();

    public static void launchMenu(){
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Main.setPrimaryStage(primaryStage);
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainMenuController.class.getResource("/GUIFiles/MainMenu.fxml"));
            AnchorPane gameView = loader.load();
            Scene scene = new Scene(gameView);
            scene = Main.loadMainCSS(scene);

            primaryStage.setTitle("Makao PCJavaFX");
            primaryStage.setScene(scene);
            primaryStage.show();
        }catch(IOException e){
            Logger.logError(e);
        }
    }

    @FXML
    public void startMultiplayerGame(){
        System.out.println("Starting loading multiplayer game");
        Stage stage = Main.getPrimaryStage();
        try{
            LoadApplication loadApplication =
                    new LoadApplication("1280x720", tfIP.getText(), tfPort.getText(), tfNickname.getText());
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(MainMenuController.class.getResource("/GUIFiles/LoadingScreen.fxml"));
            fxmlLoader.setController(loadApplication);
            AnchorPane loadingScreen = fxmlLoader.load();
            Scene scene = new Scene(loadingScreen);
            scene = Main.loadMainCSS(scene);

            stage.setTitle("Makao PCJavaFX - loading game");
            stage.setScene(scene);
            stage.show();
            loadApplication.initializeConnection();
        }catch(IOException e){
            Logger.logError(e);
        }
    }

    @FXML
    public void showMainMenu(){
        apAnotherOptions.setVisible(false);
        apMainMenu.setVisible(true);
        apSingleplayer.setVisible(false);
        apMultiplayer.setVisible(false);
        apHighscore.setVisible(false);
        apSettings.setVisible(false);
        apAuthor.setVisible(false);
    }

    @FXML
    public void showMultiplayer(){
        apMainMenu.setVisible(false);
        apMultiplayer.setVisible(true);
        apAnotherOptions.setVisible(true);
    }

    @FXML
    public void exitGame(){
        System.exit(0);
    }
}
