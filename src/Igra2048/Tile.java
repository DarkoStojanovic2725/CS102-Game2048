/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Igra2048;

import java.util.Random;
import javafx.scene.control.Label;
import java.util.Optional;
import javafx.geometry.Pos;
/**
 *
 * @author darko
 */
public class Tile extends Label{
    
    private Integer value;
    private Location location;
    private Boolean merged;
    
    
    
    public static Tile newRandomTile(){
        int value = new Random().nextDouble() < 0.9 ? 2:4;
        return new Tile(value);
    }
    
    public static Tile newTile(int value){
         return new Tile(value);
   }
    
   private Tile(Integer value){
       final int squareSize = Table.CELL_VELICINA - 13;
       setMinSize(squareSize, squareSize);
       setMaxSize(squareSize,squareSize);
       setPrefSize(squareSize, squareSize);
       setAlignment(Pos.CENTER);
       
       this.value = value;
       this.merged = false;
       setText(value.toString());
       getStyleClass().addAll("game-labela", "game-tile-" + value);
   } 
   
   public void merge(Tile anot){
       getStyleClass().remove("game-tile-" + value);
       this.value += anot.getValue();
       setText(value.toString());
       merged = true;
       getStyleClass().add("game-tile-" + value);
   }
   
   public Integer getValue(){
       return value;
   }
   
   public Location getLocation(){
       return location;
   }
   
   public void setLocation(Location location){
       this.location = location;
   }
   
   public String toString(){
       return "Tile{" + "value=" + value + ", location=" + location + '}';
   }
   
   public Boolean isMerged(){
       return merged;
   }
   
   public void clearMerge(){
       merged = false;
   }
   
   public boolean isMergeable(Optional<Tile> anotTile){
       return anotTile.filter(t -> t.getValue().equals(getValue())).isPresent();
   }
}
