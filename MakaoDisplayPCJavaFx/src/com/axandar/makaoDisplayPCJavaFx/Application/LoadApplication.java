package com.axandar.makaoDisplayPCJavaFx.Application;

/**
 * Created by Axandar on 08.03.2016.
 */

import com.axandar.makaoClient.Client;
import com.axandar.makaoClient.ClientProperties;
import com.axandar.makaoCore.utils.Logger;
import com.axandar.makaoDisplayPCJavaFx.Application.Controllers.GameController;
import com.axandar.makaoDisplayPCJavaFx.Main;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.Notifications;

public class LoadApplication{

    private String TAG = "Loading application";

    private String gameResolution;
    private String ip;
    private String port;
    private String nickname;
    private ClientProperties cProperties;

    public LoadApplication(String _gameResolution, String _ip, String _port, String _nickname){
        gameResolution = _gameResolution;
        ip = _ip;
        port = _port;
        nickname = _nickname;
    }

    public void initializeConnection(){
        cProperties = new ClientProperties();
        cProperties.setIp(ip);
        cProperties.setPort(Integer.parseInt(port));
        cProperties.setNickname(nickname);

        Client client = new Client(cProperties);

        Runnable showNotification = () -> {
            Notifications.create().title("Connection error")
                    .text("The program has encountered a problem connecting to the server").showWarning();
        };

        Runnable handlingConnection = () -> {
            Thread clientThread = new Thread(client);
            clientThread.start();
        };

        Task handlingConnectionTask = new Task(){
            @Override
            protected Object call() throws Exception{
                Platform.runLater(handlingConnection);
                return null;
            }
        };

        Thread connectionThread = new Thread(handlingConnectionTask);
        connectionThread.start();

        Runnable launchGameGUI = this::launchGameGUI;

        Task isConnectionGood = new Task(){
            @Override
            protected Object call() throws Exception{
                if(!isTimeout(cProperties)){
                    Platform.runLater(launchGameGUI);
                }else{
                    Platform.runLater(showNotification);
                    // TODO: 05.05.2016 return to main menu, maybe after notification?
                }
                return null;
            }
        };

        Thread loadingThread = new Thread(isConnectionGood);
        loadingThread.start();
    }

    private boolean isTimeout(ClientProperties cProperties){
        int timeout = 20;
        while(!cProperties.isClientRunning()){
            try{
                Thread.sleep(500);
                timeout--;
            }catch(InterruptedException e){
                Logger.logError(e);
            }
            if(timeout <= 0){
                return true;
            }
        }
        return false;
    }

    public void launchGameGUI(){
        try{
            GameController gameController = new GameController(cProperties);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(GameController.class.getResource("/GUIFiles/Game" + gameResolution + ".fxml"));
            loader.setController(gameController);
            BorderPane gameView = loader.load();

            Scene scene = new Scene(gameView);
            scene = Main.loadMainCSS(scene);
            scene.getStylesheets().add(GameController.class.getResource("/CSS/cardsInHand.css").toExternalForm());

            Main.getPrimaryStage().setTitle("Makao PCJavaFX " + gameResolution);
            Main.getPrimaryStage().setScene(scene);
            Main.getPrimaryStage().show();
        }catch(Exception e){
            Logger.logError(e);
        }
    }
}
