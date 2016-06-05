package com.axandar.makaoCore.logic;

import java.io.Serializable;

/**
 * Created by Axandar on 11.04.2016.
 */
public class StopMakao implements Serializable{
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
