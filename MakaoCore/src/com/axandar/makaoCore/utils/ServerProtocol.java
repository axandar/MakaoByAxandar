package com.axandar.makaoCore.utils;

/**
 * Created by Axandar on 06.02.2016.
 */
public class ServerProtocol{

    //Server commands
    public static int GOT_CMD = 0x001;
    public static int GOT_CARD = 0x002;
    public static int GOT_ORDER_CARD = 0x003;
    public static int GOT_ORDERED_CARD = 0x004;
    public static int START_UPDATE = 0x005;
    public static int STOP_UPDATE = 0x006;
    public static int IS_SAID_STOPMAKAO = 0x007;

    //Game commands
    public static int ACCEPTED_NICK = 0x101;
    public static int GAME_STARTED = 0x102;
    public static int GAME_ENDED = 0x103;


    //Player commands
    public static int TURN_STARTED = 0x201;
    public static int TURN_ENDED = 0x202;
    public static int CARD_ACCEPTED = 0x203;
    public static int CARD_NOTACCEPTED = 0x204;
    public static int PLAYER_SET_MAKAO = 0x205;
    public static int PLAYER_CANCEL_MAKAO = 0x206;
    public static int PLAYER_SAID_STOPMAKAO = 0x207;
    public static int PLAYER_NOT_SAID_STOPMAKAO = 0x208;//sending

    //Client commands
    public static int SEND_CARD_NORMAL = 0x301;
    //public static int SEND_CARD_ORDER = 0x302;

    //Errors
    public static int PLAYER_DISCONNECTED = 0x402;


}
