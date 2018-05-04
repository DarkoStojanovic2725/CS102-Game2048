/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Igra2048;

import java.io.IOException;
import javafx.util.Duration;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
/**
 *
 * @author darko
 */
public class Table extends Group{
    public static final int CELL_VELICINA = 128;
    private static final int SIRINA_GRANICE = (14+2)/2;
    private static final int TOP_VISINA = 92;
    private static final int GAP_VISINA = 50;
    private static final int TOOLBAR_VISINA = 80;
    
    TextField tf1 = new TextField();
    
    private final IntegerProperty gameScoreProp = new SimpleIntegerProperty(0);
    private final IntegerProperty bestScoreProp = new SimpleIntegerProperty(0);
    private final IntegerProperty gameMovePoints = new SimpleIntegerProperty(0);
    private final BooleanProperty wonProp = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOverProp = new SimpleBooleanProperty(false);
    private final BooleanProperty aboutProp = new SimpleBooleanProperty(false);
    private final BooleanProperty pauseProp = new SimpleBooleanProperty(false);
    private final BooleanProperty tryAgainProp = new SimpleBooleanProperty(false);
    private final BooleanProperty saveProp = new SimpleBooleanProperty(false);
    private final BooleanProperty restoreProp = new SimpleBooleanProperty(false);
    private final BooleanProperty quitProp = new SimpleBooleanProperty(false);
    private final BooleanProperty layerOnProp = new SimpleBooleanProperty(false);
    private final BooleanProperty resetGame = new SimpleBooleanProperty(false);
    private final BooleanProperty clearGame = new SimpleBooleanProperty(false);
    private final BooleanProperty restoreGame = new SimpleBooleanProperty(false);
    private final BooleanProperty saveGame = new SimpleBooleanProperty(false);

    
    private LocalTime vreme;
    private Timeline tajmer;
    private final StringProperty sat = new SimpleStringProperty("00:00:00");
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    
    
    //UI
    private final VBox vbGame = new VBox(0);
    private final Group gridGr = new Group();
    
    private final HBox hbTop = new HBox(0);
    private final VBox vbScore = new VBox(-5);
    private final Label lbScore = new Label("0");
    private final Label lbBestScore = new Label("0");
    private final Label lbPoeni = new Label();
    
    private final HBox overlay = new HBox();
    private final VBox textOverlay = new VBox();
    private final Label lbOverText = new Label();
    private final Label lbOverSubText = new Label();
    private final HBox buttonOverlay = new HBox();
    private final Button btTryAgain = new Button("Pokusaj ponovo");
    private final Button btContinue = new Button("Nastavi");
    private final Button btContinueSec = new Button("Ne, nastavi sa igrom");
    private final Button btSave = new Button("Sacuvaj");
    private final Button btRestore = new Button("Restore");
    private final Button btQuit = new Button("Izlaz");
    
    private final HBox hbToolbar = new HBox();
    private HostServices hostService;
    
    private final Label lbTime = new Label();
    private Timeline tajmerPauza;
    
    private final int gridSirina;
    private final GridOperator gridOperator;
    private final SessionManager sessionMng;

    public Table(GridOperator grid){
        
        this.gridOperator = grid;
        gridSirina = CELL_VELICINA * grid.getGridSize() + SIRINA_GRANICE * 2;
        sessionMng = new SessionManager(gridOperator);

        napraviScore();
        napraviGrid();
        initGameProp();
        
    }
    
    
    private void napraviScore(){
        
        Label lbTitle = new Label("Igra 2048");
        lbTitle.getStyleClass().addAll("game-labela" , "game-title");
        //subtitle nema
        
        HBox hbFill = new HBox();
        HBox.setHgrow(hbFill, Priority.ALWAYS);
        hbFill.setAlignment(Pos.CENTER);
        
        VBox vbScores = new VBox();
        HBox hbScores = new HBox(5);
        
        vbScore.setAlignment(Pos.CENTER);
        vbScore.getStyleClass().add("game-vbox");
        Label lbTitl = new Label("REZULTAT");
        lbTitl.getStyleClass().addAll("game-labela" , "game-titlRez");
        lbScore.getStyleClass().addAll("game-labela" , "game-score");
        lbScore.textProperty().bind(gameScoreProp.asString());
        vbScore.getChildren().addAll(lbTitl, lbScore);
        
        VBox vbRecord = new VBox(-5);
        vbRecord.setAlignment(Pos.CENTER);
        vbRecord.getStyleClass().add("game-vbox");
        Label lbTitlBest = new Label("Najbolji Rezultat");
        lbTitlBest.getStyleClass().addAll("game-labela" , "game-titlRez");
        lbBestScore.getStyleClass().addAll("game-labela" , "game-score");
        lbBestScore.textProperty().bind(bestScoreProp.asString());
        vbRecord.getChildren().addAll(lbTitlBest, lbBestScore);
        hbScores.getChildren().addAll(vbScore, vbRecord);
        
        VBox vbFill = new VBox();
        VBox.setVgrow(vbFill, Priority.ALWAYS);
        vbScores.getChildren().addAll(hbScores,vbFill);
        
        hbTop.getChildren().addAll(lbTitle,hbFill,vbScores);
        hbTop.setMinSize(gridSirina, TOP_VISINA);
        hbTop.setPrefSize(gridSirina, TOP_VISINA);
        hbTop.setMaxSize(gridSirina, TOP_VISINA);
        
        vbGame.getChildren().add(hbTop);
        
        
        HBox hbTime = new HBox();
        hbTime.setMinSize(gridSirina, GAP_VISINA);
        hbTime.setAlignment(Pos.BOTTOM_RIGHT);
        lbTime.getStyleClass().addAll("game-labela" , "game-vreme");
        lbTime.textProperty().bind(sat);
        tajmer = new Timeline(new KeyFrame(Duration.ZERO, e->{
            sat.set(LocalTime.now().minusNanos(vreme.toNanoOfDay()).format(format));
        }), new KeyFrame(Duration.seconds(1)));
        tajmer.setCycleCount(Animation.INDEFINITE);
        hbTime.getChildren().add(lbTime);
        
        vbGame.getChildren().add(hbTime);
        getChildren().add(vbGame);
        
        lbPoeni.getStyleClass().addAll("game-labela" , "game-poeni");
        lbPoeni.setAlignment(Pos.CENTER);
        lbPoeni.setMinWidth(100);
        getChildren().add(lbPoeni);
        
    }

    private Rectangle praviCell(int i , int j){
        final double velicinaLuka = CELL_VELICINA / 6d;
        Rectangle cell = new Rectangle(i * CELL_VELICINA, j * CELL_VELICINA,CELL_VELICINA,CELL_VELICINA);
        cell.setFill(Color.WHITE);
        cell.setStroke(Color.GREY);
        cell.setArcHeight(velicinaLuka);
        cell.setArcWidth(velicinaLuka);
        cell.getStyleClass().add("game-grid-cell");
        return cell;
    }
    
    private void napraviGrid(){
        
        gridOperator.traverseGrid((i, j) -> {
           gridGr.getChildren().add(praviCell(i, j));
           return 0;
        });
        
        gridGr.getStyleClass().add("game-grid");
        gridGr.setManaged(false);
        gridGr.setLayoutX(SIRINA_GRANICE);
        gridGr.setLayoutY(SIRINA_GRANICE);
        
        
        HBox hbBottom = new HBox();
        hbBottom.getStyleClass().add("game-backGrid");
        hbBottom.setMinSize(gridSirina, gridSirina);
        hbBottom.setPrefSize(gridSirina,gridSirina);
        hbBottom.setMaxSize(gridSirina, gridSirina);
        
        Rectangle rect = new Rectangle(gridSirina,gridSirina);
        hbBottom.setClip(rect);
        hbBottom.getChildren().add(gridGr);
        
        vbGame.getChildren().add(hbBottom);
        
        
        HBox hbPadding = new HBox();
        hbPadding.setMinSize(gridSirina, TOOLBAR_VISINA);
        hbPadding.setPrefSize(gridSirina, TOOLBAR_VISINA);
        hbPadding.setMaxSize(gridSirina, TOOLBAR_VISINA);
        
        hbToolbar.setAlignment(Pos.CENTER);
        hbToolbar.getStyleClass().add("game-backGrid");
        hbToolbar.setMinSize(gridSirina, TOOLBAR_VISINA);
        hbToolbar.setPrefSize(gridSirina, TOOLBAR_VISINA);
        hbToolbar.setMaxSize(gridSirina, TOOLBAR_VISINA);
        
        
        vbGame.getChildren().add(hbPadding);
        vbGame.getChildren().add(hbToolbar);
    }
    

    public void setToolBar(HBox toolbar){
        toolbar.disableProperty().bind(layerOnProp);
        toolbar.spacingProperty().bind(Bindings.divide(vbGame.widthProperty(), 10));
        hbToolbar.getChildren().add(toolbar);
        
    }
    
    public void tryAgain(){
        if(tryAgainProp.get()){
            tryAgainProp.set(true);
        }
    }
    
    private void btnTryAgain(){
        tajmerPauza.stop();
        layerOnProp.set(false);
        doResetGame();
    }
    
    private void keepGoing(){
        tajmerPauza.stop();
        layerOnProp.set(false);
        pauseProp.set(false);
        tryAgainProp.set(false);
        saveProp.set(false);
        restoreProp.set(false);
        aboutProp.set(false);
        quitProp.set(false);
        tajmer.play();
    }
    
    private void quit(){
        tajmerPauza.stop();
        Platform.exit();
    }
    
    private final Overlay wonListener = new Overlay("Pobedili ste!","",btContinueSec,btTryAgain,"game-overlay-won", "game-lblwon",true);
    
    private class Overlay implements ChangeListener<Boolean>{
        
        private final Button bt1, bt2;
        private final String message, warning;
        private final String stil1, stil2;
        private final boolean pauza;
        
        public Overlay(String message,String warning, Button bt1, Button bt2,String stil1, String stil2, boolean pauza){
            this.message = message;
            this.warning = warning;
            this.bt1 = bt1;
            this.bt2 = bt2;
            this.stil1 = stil1;
            this.stil2 = stil2;
            this.pauza = pauza;
        }
        @Override
        public void changed(ObservableValue<? extends Boolean> observable,Boolean oldValue,Boolean newValue){
            if(newValue){
                tajmer.stop();
                if(pauza){
                    tajmerPauza.play();
                }
                overlay.getStyleClass().setAll("game-overlay", stil1);
                lbOverText.setText(message);
                lbOverText.getStyleClass().setAll("game-labela", stil2);
                lbOverSubText.setText(warning);
                lbOverSubText.getStyleClass().setAll("game-labela", "game-lbWarning");
                textOverlay.getChildren().setAll(lbOverText,lbOverSubText);
                buttonOverlay.getChildren().setAll(bt1);
                if(bt2 != null){
                    buttonOverlay.getChildren().add(bt2);
                }
                if(!layerOnProp.get()){
                    Table.this.getChildren().addAll(overlay,buttonOverlay);
                    layerOnProp.set(true);
                }
            }
        }
        
    }
    
    private void initGameProp(){
        overlay.setMinSize(gridSirina, gridSirina);
        overlay.setAlignment(Pos.CENTER);
        overlay.setTranslateY(TOP_VISINA + GAP_VISINA);
        
        overlay.getChildren().setAll(textOverlay);
        textOverlay.setAlignment(Pos.CENTER);
        
        buttonOverlay.setAlignment(Pos.CENTER);
        buttonOverlay.setTranslateY(TOP_VISINA + GAP_VISINA + gridSirina / 2);
        buttonOverlay.setMinSize(gridSirina, gridSirina / 2);
        buttonOverlay.setSpacing(10);
        
        btTryAgain.getStyleClass().add("game-button");
        btTryAgain.setOnTouchPressed(e-> btnTryAgain());
        btTryAgain.setOnAction(e -> btnTryAgain());
        btTryAgain.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                btnTryAgain();
            }
        });
        
        btContinue.getStyleClass().add("game-button");
        btContinue.setOnTouchPressed(e -> keepGoing());
        btContinue.setOnMouseClicked(e -> keepGoing());
        btContinue.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                keepGoing();
            }
        });
       
        btContinueSec.getStyleClass().add("game-button");
        btContinueSec.setOnTouchPressed(e -> keepGoing());
        btContinueSec.setOnMouseClicked(e -> keepGoing());
        btContinueSec.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                keepGoing();
            }
        });
        
        btSave.getStyleClass().add("game-button");
        btSave.setOnTouchPressed(e -> saveGame.set(true));
        btSave.setOnMouseClicked(e -> saveGame.set(true));
        btSave.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                saveGame.set(true);
            }
        });
        
        btRestore.getStyleClass().add("game-button");
        btRestore.setOnTouchPressed(e -> restoreGame.set(true));
        btRestore.setOnMouseClicked(e -> restoreGame.set(true));
        btRestore.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                restoreGame.set(true);
            }
        });
        
        btQuit.getStyleClass().add("game-button");
        btQuit.setOnTouchPressed(e -> quit());
        btQuit.setOnMouseClicked(e -> quit());
        btQuit.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)){
                quit();
            }
        });
        
        tajmerPauza = new Timeline(new KeyFrame(Duration.seconds(1),
        e -> vreme = vreme.plusNanos(1_000_000_000)));
        tajmerPauza.setCycleCount(Animation.INDEFINITE);
        
        wonProp.addListener(wonListener);
        gameOverProp.addListener(new Overlay("Igra je zavrsena!", "", btTryAgain, null, "game-overlay-over","game-lbOver", false));
        pauseProp.addListener(new Overlay("Igra je pauzirana", "", btContinue, null, "game-overlay-pause", "game-lbPause",true));
        tryAgainProp.addListener(new Overlay("Pokusaj ponovo?", "Trenutni progres ce biti obrisan",btTryAgain,btContinueSec,"game-overlay-pause","game-lbPause",true));
        saveProp.addListener(new Overlay("Sacuvaj?","Stari podaci ce biti obrisani",btSave,btContinueSec,"game-overlay-pause","game-lbPause",true));
        restoreProp.addListener(new Overlay("Restore?","Trenutni progres ce biti obrisan",btRestore,btContinueSec,"game-overlay-pause","game-lbPause",true));
        aboutProp.addListener((observable,oldValue, newValue) -> {
           if(newValue){
               tajmer.stop();
               tajmerPauza.play();
               overlay.getStyleClass().setAll("game-overlay","game-overlay-quit");
               TextFlow fl = new TextFlow();
               fl.setTextAlignment(TextAlignment.CENTER);
               fl.setPadding(new Insets(10,0,0,0));
               fl.setMinSize(gridSirina, gridSirina);
               fl.setPrefSize(gridSirina, gridSirina);
               fl.setMaxSize(gridSirina,gridSirina);
               fl.setPrefSize(BASELINE_OFFSET_SAME_AS_HEIGHT, BASELINE_OFFSET_SAME_AS_HEIGHT);
               Text t1 = new Text("Igra");
               t1.getStyleClass().setAll("game-labela", "game-lbAbout");
               Text t2 = new Text("2048");
               t2.getStyleClass().setAll("game-labela", "game-lbAbout2");
               Text t3 = new Text(" About\n");
               t3.getStyleClass().setAll("game-labela", "game-lbAbout");
               Text t4 = new Text("Igra 2048 - Projekat CS102\n\n");
               t4.getStyleClass().setAll("game-labela", "game-lbAboutSub");
               Text t5 = new Text("Igru napravio: ");
               t5.getStyleClass().setAll("game-labela", "game-lbAboutSub");
               Text t6 = new Text("Darko Stojanovic");
               t6.getStyleClass().setAll("game-labela", "game-lbAboutSub");
               
              tf1.getStyleClass().setAll("game-labela","game-lbAboutSub");
               try {
                   instruction();
               } catch (IOException ex) {
                   Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
               }
              
               Text traz = new Text("\n\n");
               traz.getStyleClass().setAll("game-labela", "game-lbAboutSub");
               
               Text t7 = new Text(" Verzija " +Igra2048.VERZIJA+ "");
               t7.getStyleClass().setAll("game-labela", "game-lbAboutSub");
               
               fl.getChildren().setAll(t1,t2,t3,t4,t5,t6);
               fl.getChildren().addAll(traz,t7,tf1);
               textOverlay.getChildren().setAll(fl);
               buttonOverlay.getChildren().setAll(btContinue);
               this.getChildren().removeAll(overlay, buttonOverlay);
               this.getChildren().addAll(overlay, buttonOverlay);
               layerOnProp.set(true);
               
           } 
        });
        
        quitProp.addListener(new Overlay("Izadji iz igre?", "Nesacuvani podaci ce biti izgubljeni", btQuit,btContinueSec,"game-overlay-quit","game-lbQuit",true));
        
        restoreRecord();
        gameScoreProp.addListener((ov,i,i1) -> {
            if(i1.intValue() > bestScoreProp.get()){
                bestScoreProp.set(i1.intValue());
            }
        });
        
        layerOnProp.addListener((ov,b,b1) -> {
            if(!b1){
                getChildren().removeAll(overlay,buttonOverlay);
                getParent().requestFocus();
            }else if(b1){
                buttonOverlay.getChildren().get(0).requestFocus();
            }
        });
    }
    
    public void setHostServices(HostServices hostServices){
        this.hostService = hostServices;
    }
    
    private void doClearGame(){
        saveRecord();
        gridGr.getChildren().removeIf(c -> c instanceof Tile);
        getChildren().removeAll(overlay, buttonOverlay);
        
        clearGame.set(false);
        resetGame.set(false);
        restoreGame.set(false);
        saveGame.set(false);
        layerOnProp.set(false);
        gameScoreProp.set(0);
        wonProp.set(false);
        gameOverProp.set(false);
        aboutProp.set(false);
        pauseProp.set(false);
        tryAgainProp.set(false);
        saveProp.set(false);
        restoreProp.set(false);
        quitProp.set(false);
        
        clearGame.set(true);
        
    }
    
    private void doResetGame(){
        doClearGame();
        resetGame.set(true);
    }
    
    public void animateScore(){
        if(gameMovePoints.get() == 0){
            return;
        }
        
        final Timeline timeline = new Timeline();
        lbPoeni.setText("+" + gameMovePoints.getValue().toString());
        lbPoeni.setOpacity(1);
        double posX = vbScore.localToScene(vbScore.getWidth()/2d, 0).getX();
        lbPoeni.setTranslateX(0);
        lbPoeni.setTranslateX(lbPoeni.sceneToLocal(posX,0).getX() - lbPoeni.getWidth()/2d);
        lbPoeni.setLayoutY(20);
        final KeyValue kvO = new KeyValue(lbPoeni.opacityProperty(), 0);
        final KeyValue kvY = new KeyValue(lbPoeni.layoutYProperty(), 100);
        
        Duration animationDuration = Duration.millis(600);
        final KeyFrame kfO = new KeyFrame(animationDuration,kvO);
        final KeyFrame kfY = new KeyFrame(animationDuration, kvY);
        
        
        timeline.getKeyFrames().add(kfO);
        timeline.getKeyFrames().add(kfY);
        
        timeline.play();
        
    }
    
    
    public void addTile(Tile tile){
        
        double layoutX = tile.getLocation().getLayoutX(CELL_VELICINA) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_VELICINA) - (tile.getMinHeight() / 2);
        
        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        gridGr.getChildren().add(tile);

    }
    
    public Tile addRandomTile(Location randomLocation){
        Tile tile = Tile.newRandomTile();
        tile.setLocation(randomLocation);
        
        double layoutX = tile.getLocation().getLayoutX(CELL_VELICINA) - (tile.getMinWidth() / 2);
        double layoutY = tile.getLocation().getLayoutY(CELL_VELICINA) - (tile.getMinHeight() / 2);
        
        tile.setLayoutX(layoutX);
        tile.setLayoutY(layoutY);
        tile.setScaleX(0);
        tile.setScaleY(0);
        
        gridGr.getChildren().add(tile);
        
        return tile;
    }
    
    public Group getGridGroup(){
        return gridGr;
    }
    
    public void startGame(){
        restoreRecord();
        
        vreme = LocalTime.now();
        tajmer.playFromStart();
    }
    
    public void setPoeni(int poeni){
        gameMovePoints.set(poeni);
    }
    
    public int getPoeni(){
        return gameMovePoints.get();
    }
    
    public void addPoeni(int poeni){
        gameMovePoints.set(gameMovePoints.get() + poeni);
        gameScoreProp.set(gameScoreProp.get() + poeni);
    }
    
    public void setGameOver(boolean kraj){
        gameOverProp.set(kraj);
    }
    
    public void setGameWin(boolean won){
        if(wonProp.get()){
            wonProp.set(won);
        }
    }
    
    public void pauseGame(){
        if(!pauseProp.get()){
            pauseProp.set(true);
        }
    }
    
    public void aboutGame(){
        if(!aboutProp.get()){
            aboutProp.set(true);
        }
    }
    
    public void quitGame(){
        if(!quitProp.get()){
            quitProp.set(true);
        }
    }
    public BooleanProperty isLayerOn(){
        return layerOnProp;
    }
    
    public BooleanProperty resetGameProp(){
    return resetGame;
        
}
    
    public BooleanProperty clearGameProp(){
        return clearGame;
    }
    
    public BooleanProperty saveGameProp(){
        return saveGame;
        
    }
    
    
    public BooleanProperty restoreGameProp(){
        return restoreGame;
    }
    
    
    public boolean saveSession(){
        if(!saveProp.get()){
            saveProp.set(true);
        }
        return true;
    }
    
    
    public void saveSession(Map<Location, Tile> gameGrid){
        saveGame.set(false);
        sessionMng.saveSession(gameGrid, gameScoreProp.getValue(), LocalTime.now().minusNanos(vreme.toNanoOfDay()).toNanoOfDay());
        keepGoing();
    }
    
    public boolean restoreSession(){
        if(!restoreProp.get()){
            restoreProp.set(true);
        }
        return true;
    }
    
    public boolean restoreSession(Map<Location, Tile> gameGrid){
        tajmerPauza.stop();
        restoreGame.set(false);
        doClearGame();
        tajmer.stop();
        StringProperty sVreme = new SimpleStringProperty("");
        int score = sessionMng.restoreSession(gameGrid, sVreme);
        if(score >= 0){
            gameScoreProp.set(score);
            wonProp.set(false);
            gameGrid.forEach((l, t) -> {
                if(t!=null && t.getValue() >= GameManager.KONACNA_VREDNOST){
                    wonProp.removeListener(wonListener);
                    wonProp.set(true);
                    wonProp.addListener(wonListener);
                }
                
            });
            if(!sVreme.get().isEmpty()){
                vreme = LocalTime.now().minusNanos(new Long(sVreme.get()));
            }
            tajmer.play();
            return true;
            
        }
        
        doResetGame();
        return false;
    }
    
    
    public void saveRecord(){
        RecordManager recordMng = new RecordManager(gridOperator.getGridSize());
        recordMng.saveRecord(gameScoreProp.getValue());
    }
        
    
    private void restoreRecord(){
        RecordManager recordMng = new RecordManager(gridOperator.getGridSize());
        bestScoreProp.set(recordMng.restoreRecord());
        
    }
    
    private void instruction() throws IOException{
       String url = "http://www.2048game.mobi/howtoplay.htm";
       Document doc = Jsoup.connect(url).get();
       doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
    doc.select("br").append("\\n");
    doc.select("p").prepend("\\n\\n");
    String s = doc.html().replaceAll("\\\\n", "\n");
    Elements links = doc.select("body > div > p:nth-child(5)");
    Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        for(Element e: links){
        tf1.setText(e.text());
        }

        
        
    }
    
    
}
