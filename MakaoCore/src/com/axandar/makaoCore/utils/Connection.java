package com.axandar.makaoCore.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Axandar on 20.04.2016.
 */
public class Connection{

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private int counter = 5;

    public Connection(ObjectInputStream _objInputStr, ObjectOutputStream _objOutputStr){
        objectInputStream = _objInputStr;
        objectOutputStream = _objOutputStr;
    }

    public void send(Object object){
        try{
            objectOutputStream.writeUnshared(object);
            objectOutputStream.reset();
        }catch(IOException e){
            if(counter > 0){
                counter--;
                send(object);
            }else{
                // TODO: 18.05.2016 reset connection or close client/server
                Logger.logError(e);
            }
        }
        counter = 5;
    }

    public Object receive(){
        try{
            return objectInputStream.readUnshared();
        }catch(IOException e){
            if(counter > 0){
                counter--;
                return receive();
            }else{
                // TODO: 18.05.2016 reset connection or close client/server
                Logger.logError(e);
            }
        }catch(ClassNotFoundException e){
            Logger.logError(e);
        }
        counter = 5;
        return null;
    }

    public boolean close(){
        try{
            objectInputStream.close();
            objectInputStream.close();
            return true;
        }catch(IOException e){
            Logger.logError(e);
            return false;
        }
    }
}
