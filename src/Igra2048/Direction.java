/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Igra2048;

import javafx.scene.input.KeyCode;

/**
 *
 * @author darko
 */
public enum Direction {
    UP(0, -1), RIGHT(1,0), DOWN(0,1), LEFT(-1,0);
    
    
    private final int y;
    private final int x;
    
    Direction(int x, int y){
        this.y = y;
        this.x = x;
    }
    
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    
    public String toString(){
        return "Direction{" + "y=" + y + ", x=" + x + '}' + name();
    }
    
    public static Direction valueFor(KeyCode keyCode){
        return valueOf(keyCode.name());
    }
    
}
