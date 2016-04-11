package com.axandar.makaoServer.ErrorHandling;

import com.axandar.makaoCore.logic.Player;

/**
 * Created by Axandar on 11.04.2016.
 */
public class ConnectError{
    private Player who;
    private int reason;
    private int command;

    public ConnectError(Player _who, int _reason, int _command){
        who = _who;
        reason = _reason;
        command = _command;
    }

    public Player getWho(){
        return who;
    }

    public int getReason(){
        return reason;
    }

    public int getCommand(){
        return command;
    }
}
