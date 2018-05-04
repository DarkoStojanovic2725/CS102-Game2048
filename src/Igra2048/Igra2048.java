/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Igra2048;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author darko
 */
public class Igra2048 extends Application{
    
    public final static String VERZIJA = "1.0.0";
    
    
    private GamePane root;

    @Override
    public void start(Stage primaryStage) {
        root = new GamePane();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("Igra2048/Stilovi.css");
        root.setStyle("-fx-background-image: url('res/slika.png')");

        Bounds gameBounds = root.getGameManager().getLayoutBounds();
        int MARGIN = GamePane.getMargin();
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double factor = Math.min(visualBounds.getWidth() / (gameBounds.getWidth() + MARGIN),
                visualBounds.getHeight() / (gameBounds.getHeight() + MARGIN));
        primaryStage.setTitle("Igra 2048");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(gameBounds.getWidth() / 2d);
        primaryStage.setMinHeight(gameBounds.getHeight() / 2d);
        primaryStage.setWidth((gameBounds.getWidth() + MARGIN) * factor);
        primaryStage.setHeight((gameBounds.getHeight() + MARGIN) * factor);
        
        primaryStage.setOnCloseRequest(t->{
            t.consume();
            root.getGameManager().quitGame();
        });
        primaryStage.show();
    }

    @Override
    public void stop() {
        root.getGameManager().saveRecord();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
