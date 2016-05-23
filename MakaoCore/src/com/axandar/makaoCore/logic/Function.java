package com.axandar.makaoCore.logic;

import java.io.Serializable;

/**
 * Created by Axandar on 25.01.2016.
 */
public class Function implements Serializable{

    public static final int GET_CARDS_FORWARD = 0;
    public static final int GET_CARDS_BACKWARD = 1;
    public static final int WAIT_TURNS = 2;
    public static final int ORDER_CARD = 3;
    public static final int CAMELEON_CARD = 4;
    public static final int CHANGE_COLOR = 5;
    public static final int NOTHING = 6;

    private int function;
    private int value;

    public Function(int _function, int _value){
        function = _function;
        value = _value;
    }

    public int getFunctionID(){
        return function;
    }

    public int getFunctionValue(){
        return value;
    }
}
