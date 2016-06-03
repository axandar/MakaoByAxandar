package com.axandar.makaoCore.logic;

import java.io.Serializable;

/**
 * Created by Axandar on 25.01.2016.
 */

public class Card implements Serializable{

    public static final int CARD_ACE = 1;
    public static final int CARD_2 = 2;
    public static final int CARD_3 = 3;
    public static final int CARD_4 = 4;
    public static final int CARD_5 = 5;
    public static final int CARD_6 = 6;
    public static final int CARD_7 = 7;
    public static final int CARD_8 = 8;
    public static final int CARD_9 = 9;
    public static final int CARD_10 = 10;
    public static final int CARD_WALET = 11;
    public static final int CARD_DAMA = 12;
    public static final int CARD_KING = 13;

    public static final int COLOR_KARO = 0;
    public static final int COLOR_TREFL = 1;
    public static final int COLOR_KIER = 2;
    public static final int COLOR_PIK = 3;

    private int idColor;
    private int idType;
    private Function function;

    public Card(int _idColor, int _idType, Function _function){
        idColor = _idColor;
        idType = _idType;
        function = _function;
    }

    public int getIdColor(){
        return idColor;
    }

    public void setIdColor(int idColor){
        this.idColor = idColor;
    }

    public int getIdType(){
        return idType;
    }

    public void setIdType(int idType){
        this.idType = idType;
    }

    public Function getFunction(){
        return function;
    }

    public void setFunction(Function function){
        this.function = function;
    }
}


