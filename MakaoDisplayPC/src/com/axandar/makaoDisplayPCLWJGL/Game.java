package com.axandar.makaoDisplayPCLWJGL;

import com.axandar.makaoDisplayPCLWJGL.graphic.Graphic;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_TRUE;


/**
 * Created by Axandar on 16.01.2016.
 */
public class Game {

    private int updatesPerSecond = 25;
    private int updateInterval = 1000 / updatesPerSecond * 1000000;
    private int maxFrameskip = 5;

    private long window;

    public Game(){

    }

    public void start(){
        initialize();
        gameLoop();
        ending();
    }

    private void initialize(){
        //create table, cards, players
    }

    private void gameLoop(){
        long nextUpdateTime = System.nanoTime();
        Graphic graphic = new Graphic();
        while(glfwWindowShouldClose(window) != GL_TRUE){

            int skippedFrames = 0;

            while(System.nanoTime() > nextUpdateTime && skippedFrames < maxFrameskip){
                //Update logic
                updateGameLogic();
                nextUpdateTime += updateInterval;
                skippedFrames++;
            }

            long interpolation = (long)(System.nanoTime() + updateInterval - nextUpdateTime) / updateInterval;
            graphic.render(interpolation);
        }
    }

    private void updateGameLogic() {
        //update cards in hand when turn end itd.
    }

    private void ending(){

    }

}
