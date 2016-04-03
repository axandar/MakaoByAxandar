package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Axandar on 25.01.2016.
 */
public class Main {

    private static List<List<Function>> functions = new ArrayList<>();

    public static void main(String[] args) {

        initializeFunctions();

        GameSession gs = new GameSession(2, 1, functions, 5000);
        Thread gameSessionThread = new Thread(gs);
        gameSessionThread.start();

        /*//Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        URL imageURL = Main.class.getResource("indeks.jpg");
        Image image = (new ImageIcon(imageURL, "tray icon")).getImage();
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getToolkit().createImage(getClass().getResource("icon.png"));
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resizes the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        trayIcon.displayMessage("Hello, World", "notification demo \n kkkk", TrayIcon.MessageType.NONE);**/
    }

    public static List<List<Function>> initializeFunctions(){
        List<Function> card0 = new ArrayList<>();
        card0.add(new Function(Function.CHANGE_COLOR, -1));
        card0.add(new Function(Function.CHANGE_COLOR, -1));
        card0.add(new Function(Function.CHANGE_COLOR, -1));
        card0.add(new Function(Function.CHANGE_COLOR, -1));
        functions.add(card0);

        List<Function> card1 = new ArrayList<>();
        card1.add(new Function(Function.GET_CARDS_FORWARD, 2));
        card1.add(new Function(Function.GET_CARDS_FORWARD, 2));
        card1.add(new Function(Function.GET_CARDS_FORWARD, 2));
        card1.add(new Function(Function.GET_CARDS_FORWARD, 2));
        functions.add(card1);

        List<Function> card2 = new ArrayList<>();
        card2.add(new Function(Function.GET_CARDS_FORWARD, 3));
        card2.add(new Function(Function.GET_CARDS_FORWARD, 3));
        card2.add(new Function(Function.GET_CARDS_FORWARD, 3));
        card2.add(new Function(Function.GET_CARDS_FORWARD, 3));
        functions.add(card2);

        List<Function> card3 = new ArrayList<>();
        card3.add(new Function(Function.WAIT_TURNS, 1));
        card3.add(new Function(Function.WAIT_TURNS, 1));
        card3.add(new Function(Function.WAIT_TURNS, 1));
        card3.add(new Function(Function.WAIT_TURNS, 1));
        functions.add(card3);

        List<Function> card4 = new ArrayList<>();
        card4.add(new Function(Function.NOTHING, -1));
        card4.add(new Function(Function.NOTHING, -1));
        card4.add(new Function(Function.NOTHING, -1));
        card4.add(new Function(Function.NOTHING, -1));
        functions.add(card4);

        List<Function> card5 = new ArrayList<>();
        card5.add(new Function(Function.NOTHING, -1));
        card5.add(new Function(Function.NOTHING, -1));
        card5.add(new Function(Function.NOTHING, -1));
        card5.add(new Function(Function.NOTHING, -1));
        functions.add(card5);

        List<Function> card6 = new ArrayList<>();
        card6.add(new Function(Function.NOTHING, -1));
        card6.add(new Function(Function.NOTHING, -1));
        card6.add(new Function(Function.NOTHING, -1));
        card6.add(new Function(Function.NOTHING, -1));
        functions.add(card6);

        List<Function> card7 = new ArrayList<>();
        card7.add(new Function(Function.NOTHING, -1));
        card7.add(new Function(Function.NOTHING, -1));
        card7.add(new Function(Function.NOTHING, -1));
        card7.add(new Function(Function.NOTHING, -1));
        functions.add(card7);

        List<Function> card8 = new ArrayList<>();
        card8.add(new Function(Function.NOTHING, -1));
        card8.add(new Function(Function.NOTHING, -1));
        card8.add(new Function(Function.NOTHING, -1));
        card8.add(new Function(Function.NOTHING, -1));
        functions.add(card8);

        List<Function> card9 = new ArrayList<>();
        card9.add(new Function(Function.NOTHING, -1));
        card9.add(new Function(Function.NOTHING, -1));
        card9.add(new Function(Function.NOTHING, -1));
        card9.add(new Function(Function.NOTHING, -1));
        functions.add(card9);

        List<Function> card10 = new ArrayList<>();
        card10.add(new Function(Function.ORDER_CARD, -1));
        card10.add(new Function(Function.ORDER_CARD, -1));
        card10.add(new Function(Function.ORDER_CARD, -1));
        card10.add(new Function(Function.ORDER_CARD, -1));
        functions.add(card10);

        List<Function> card11 = new ArrayList<>();
        card11.add(new Function(Function.CAMELEON_CARD, -1));
        card11.add(new Function(Function.CAMELEON_CARD, -1));
        card11.add(new Function(Function.CAMELEON_CARD, -1));
        card11.add(new Function(Function.CAMELEON_CARD, -1));
        functions.add(card11);

        List<Function> card12 = new ArrayList<>();
        card12.add(new Function(Function.GET_CARDS_FORWARD, 5));
        card12.add(new Function(Function.GET_CARDS_FORWARD, 5));
        card12.add(new Function(Function.GET_CARDS_BACKWARD, 5));
        card12.add(new Function(Function.GET_CARDS_BACKWARD, 5));
        functions.add(card12);

        return functions;
    }

}
