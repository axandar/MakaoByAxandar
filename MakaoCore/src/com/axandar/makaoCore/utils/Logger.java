package com.axandar.makaoCore.utils;

/**
 * Created by Axandar on 18.02.2016.
 */
public class Logger {

    private static String programName = "[LWJGL Makao]";

    public static void logConsole(String tag, String message){
        System.out.println(programName + " --- [" + tag + "]: " + message);
    }

    public static void logError(Exception exception){
        exception.printStackTrace();
        System.out.println(exception.getMessage());
    }
}
