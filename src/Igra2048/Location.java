/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Igra2048;

/**
 *
 * @author darko
 */
public class Location {
    private final int x;
    private final int y;
    
    public Location(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    public Location offset(Direction direction){
        return new Location(x + direction.getX(), y + direction.getY());
    }
    
    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }
    
    public String toString(){
        return "Location{" + "x=" + x + ", y=" + y +'}';
    }
    
    public int hashCode(){
    int hash = 7;
    hash = 97 * hash + this.x;
    hash = 97 * hash + this.y;
    return hash;
}
    
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }
        if(getClass()!= obj.getClass()){
            return false;
        }
        final Location other = (Location)obj;
        if(this.x != other.x){
            return false;
        }
        return this.y == other.y;
    }
    
    public double getLayoutY(int CELL_VELICINA){
        if(y == 0){
            return CELL_VELICINA / 2;
        }
            return (y * CELL_VELICINA) + CELL_VELICINA / 2;
  }
    
    
    public double getLayoutX(int CELL_VELICINA){
        if(x == 0){
            return CELL_VELICINA / 2;
        }
        return (x * CELL_VELICINA) + CELL_VELICINA / 2;
                
    }
    
    
    public boolean isValidFor(int gridVelicina){
        return x >= 0 && x < gridVelicina && y >= 0 && y < gridVelicina;
    }
    
}
