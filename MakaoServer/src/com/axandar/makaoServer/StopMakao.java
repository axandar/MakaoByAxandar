package com.axandar.makaoServer;

import com.axandar.makaoCore.logic.Player;

/**
 * Created by Axandar on 11.04.2016.
 */
public class StopMakao{
    private Player whoSaid;
    private Player toWho;

    public StopMakao(Player who, Player to){
        whoSaid = who;
        toWho = to;
    }

    public Player getWhoSaid(){
        return whoSaid;
    }

    public Player getToWho(){
        return toWho;
    }
}
