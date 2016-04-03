package com.axandar.makaoDisplayPCJavaFx.Application;

/**
 * Created by Axandar on 08.03.2016.
 */

import com.axandar.makaoCore.utils.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoadApplication extends Application{

    private String gameResolution = "1280x720";

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LoadApplication.class.getResource("GUIFiles/Game" + gameResolution + ".fxml"));
            BorderPane gameView = loader.load();
            Scene scene = new Scene(gameView);
            scene.getStylesheets().add(this.getClass().getResource("/CSS/style.css").toExternalForm());
            scene.getStylesheets().add(this.getClass().getResource("/CSS/cardsInHand.css").toExternalForm());

            primaryStage.setTitle("Makao PCJavaFX " + gameResolution);
            primaryStage.setScene(scene);
            primaryStage.show();
        }catch(IOException e){
            Logger.logError(e);
        }

    }
}
