package com.axandar.makaoDisplayPCJavaFx;

import com.axandar.makaoDisplayPCJavaFx.Application.Controllers.MainMenuController;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main{

    private static Stage primaryStage;

    public static void main(String[] args) {

        MainMenuController.launchMenu();
        //launch(args);
    }

    public static Scene loadMainCSS(Scene scene){
        scene.getStylesheets().add(Main.class.getResource("/CSS/style.css").toExternalForm());
        return scene;
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void setPrimaryStage(Stage _primaryStage){
        primaryStage = _primaryStage;
    }
}