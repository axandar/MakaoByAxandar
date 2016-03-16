package com.axandar.makaoCore.utils;

/**
 * Created by Axandar on 06.02.2016.
 */
public class ServerProtocol {
    
    //Server commands
    public static int GOT_CMD = 0x001;
    public static int GOT_CARD = 0x002;
    public static int GOT_ORDER_CARD = 0x003;
    //public static int GOT_ORDERED_CARD = 0x004;
    public static int START_UPDATE_PLAYERS = 0x005;
    public static int END_UPDATE_PLAYERS = 0x006;
    
    //Game commands
    public static int ACCEPTED_NICK = 0x0A1;
    public static int GAME_STARTED = 0x0A2;
    public static int GAME_ENDED = 0x0A3;


    //Player commands
    public static int TURN_STARTED = 0x0B1;
    public static int TURN_ENDED = 0x0B2;
    public static int CARD_ACCEPTED = 0x0B3;
    public static int CARD_NOTACCEPTED = 0x0B4;
    public static int PLAYER_SET_MAKAO = 0x0B5;
    public static int PLAYER_CANCEL_MAKAO = 0x0B5;

    //Client commands
    public static int SEND_CARD_NORMAL = 0x0C1;
    //public static int SEND_CARD_ORDER = 0x0C2;
    
}
