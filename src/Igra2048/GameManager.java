/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Igra2048;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
/**
 *
 * @author darko
 */
public class GameManager extends Group{
    
    public final static int KONACNA_VREDNOST = 2048;
    private static final Duration ANIMATION_EXISTING_TILE = Duration.millis(65);
    private static final Duration ANIMATION_NEWLY_ADDED_TILE = Duration.millis(125);
    private static final Duration ANIMATION_MERGED_TILE = Duration.millis(80);
    
    private volatile boolean movingTiles = false;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Tile> gameGrid;
    private final Set<Tile> mergedToBeRemoved = new HashSet<>();

    private final Table table;
    private final GridOperator gridOperator;

    public GameManager() {
        this(GridOperator.DEFAULT_GRID_SIZE);
    }
    
    public GameManager(int gridSize) {
        this.gameGrid = new HashMap<>();
        
        gridOperator = new GridOperator(gridSize);
        table = new Table(gridOperator);
        this.getChildren().add(table);

        table.clearGameProp().addListener((ov, b, b1) -> {
            if (b1) {
                initializeGameGrid();
            }
        });
        table.resetGameProp().addListener((ov, b, b1) -> {
            if (b1) {
                startGame();
            }
        });
        table.restoreGameProp().addListener((ov, b, b1) -> {
            if (b1) {
                doRestoreSession();
            }
        });
        table.saveGameProp().addListener((ov, b, b1) -> {
            if (b1) {
                doSaveSession();
            }
        });
        
        initializeGameGrid();
        startGame();
    }

    private void initializeGameGrid() {
        gameGrid.clear();
        locations.clear();
        gridOperator.traverseGrid((x, y) -> {
            Location thisloc = new Location(x, y);
            locations.add(thisloc);
            gameGrid.put(thisloc, null);
            return 0;
        });
    }
    private void startGame() {
        Tile tile0 = Tile.newRandomTile();
        List<Location> randomLocs = new ArrayList<>(locations);
        Collections.shuffle(randomLocs);
        Iterator<Location> locs = randomLocs.stream().limit(2).iterator();
        tile0.setLocation(locs.next());

        Tile tile1 = null;
        if (new Random().nextFloat() <= 0.8) { // gives 80% chance to add a second tile
            tile1 = Tile.newRandomTile();
            if (tile1.getValue() == 4 && tile0.getValue() == 4) {
                tile1 = Tile.newTile(2);
            }
            tile1.setLocation(locs.next());
        }

        Arrays.asList(tile0, tile1).stream().filter(Objects::nonNull)
                .forEach(t -> gameGrid.put(t.getLocation(), t));
        
        redrawTilesInGameGrid();

        table.startGame();
    }
    
  
    private void redrawTilesInGameGrid() {
        gameGrid.values().stream().filter(Objects::nonNull).forEach(t->table.addTile(t));
    }

    private void moveTiles(Direction direction) {
        synchronized (gameGrid) {
            if (movingTiles) {
                return;
            }
        }

        table.setPoeni(0);
        mergedToBeRemoved.clear();
        ParallelTransition parallelTransition = new ParallelTransition();
        gridOperator.sortGrid(direction);
        final int tilesWereMoved = gridOperator.traverseGrid((x, y) -> {
            Location thisloc = new Location(x, y);
            Location farthestLocation = findFarthestLocation(thisloc, direction); // farthest available location
            Optional<Tile> opTile = optionalTile(thisloc);
            
            AtomicInteger result=new AtomicInteger();
            Location nextLocation = farthestLocation.offset(direction); // calculates to a possible merge
            optionalTile(nextLocation).filter(t-> t.isMergeable(opTile) && !t.isMerged())
                    .ifPresent(t->{
                        Tile tile=opTile.get();
                        t.merge(tile);
                        t.toFront();
                        gameGrid.put(nextLocation, t);
                        gameGrid.replace(thisloc, null);

                        parallelTransition.getChildren().add(animateExistingTile(tile, t.getLocation()));
                        parallelTransition.getChildren().add(animateMergedTile(t));
                        mergedToBeRemoved.add(tile);

                        table.addPoeni(t.getValue());

                        if (t.getValue() == KONACNA_VREDNOST) {
                            table.setGameWin(true);
                        }
                        result.set(1);
                    });
            if (result.get()==0 && opTile.isPresent() && !farthestLocation.equals(thisloc)) {
                Tile tile=opTile.get();
                parallelTransition.getChildren().add(animateExistingTile(tile, farthestLocation));

                gameGrid.put(farthestLocation, tile);
                gameGrid.replace(thisloc, null);

                tile.setLocation(farthestLocation);

                result.set(1);
            }

            return result.get();
        });

        table.animateScore();
        if(parallelTransition.getChildren().size()>0){
            parallelTransition.setOnFinished(e -> {
                table.getGridGroup().getChildren().removeAll(mergedToBeRemoved);
                gameGrid.values().stream().filter(Objects::nonNull).forEach(Tile::clearMerge);

                Location randomAvailableLocation = findRandomAvailableLocation();
                if (randomAvailableLocation == null && mergeMovementsAvailable() == 0 ) {
                    table.setGameOver(true);
                } else if (randomAvailableLocation != null && tilesWereMoved > 0) {
                    synchronized (gameGrid) {
                        movingTiles = false;
                    }
                    addAndAnimateRandomTile(randomAvailableLocation);
                }
            });

            synchronized (gameGrid) {
                movingTiles = true;
            }

            parallelTransition.play();
        }
    }
    private Optional<Tile> optionalTile(Location loc) { 
        return Optional.ofNullable(gameGrid.get(loc)); 
    }
    
   
    private Location findFarthestLocation(Location location, Direction direction) {
        Location farthest;

        do {
            farthest = location;
            location = farthest.offset(direction);
        } while (gridOperator.isValidLocation(location) && !optionalTile(location).isPresent());

        return farthest;
    }

    private int mergeMovementsAvailable() {
        final AtomicInteger pairsOfMergeableTiles = new AtomicInteger();

        Stream.of(Direction.UP, Direction.LEFT).parallel().forEach(direction -> {
            gridOperator.traverseGrid((x, y) -> {
                Location thisloc = new Location(x, y);
                optionalTile(thisloc).ifPresent(t->{
                    if(t.isMergeable(optionalTile(thisloc.offset(direction)))){
                        pairsOfMergeableTiles.incrementAndGet();
                    }
                });
                return 0;
            });
        });
        return pairsOfMergeableTiles.get();
    }

    private Location findRandomAvailableLocation() {
        List<Location> availableLocations = locations.stream().filter(l -> gameGrid.get(l) == null)
                .collect(Collectors.toList());

        if (availableLocations.isEmpty()) {
            return null;
        }

        Collections.shuffle(availableLocations);
        Location randomLocation = availableLocations.get(new Random().nextInt(availableLocations.size()));
        return randomLocation;
    }

    private void addAndAnimateRandomTile(Location randomLocation) {
        Tile tile = table.addRandomTile(randomLocation);
        gameGrid.put(tile.getLocation(), tile);
        
        animateNewlyAddedTile(tile).play();
    }

    private ScaleTransition animateNewlyAddedTile(Tile tile) {
        final ScaleTransition scaleTransition = new ScaleTransition(ANIMATION_NEWLY_ADDED_TILE, tile);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        scaleTransition.setOnFinished(e -> {
            // after last movement on full grid, check if there are movements available
            if (this.gameGrid.values().parallelStream().noneMatch(Objects::isNull) && mergeMovementsAvailable() == 0 ) {
                table.setGameOver(true);
            }
        });
        return scaleTransition;
    }
    
    private Timeline animateExistingTile(Tile tile, Location newLocation) {
        Timeline timeline = new Timeline();
        KeyValue kvX = new KeyValue(tile.layoutXProperty(),
                newLocation.getLayoutX(Table.CELL_VELICINA) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);
        KeyValue kvY = new KeyValue(tile.layoutYProperty(),
                newLocation.getLayoutY(Table.CELL_VELICINA) - (tile.getMinHeight() / 2), Interpolator.EASE_OUT);

        KeyFrame kfX = new KeyFrame(ANIMATION_EXISTING_TILE, kvX);
        KeyFrame kfY = new KeyFrame(ANIMATION_EXISTING_TILE, kvY);

        timeline.getKeyFrames().add(kfX);
        timeline.getKeyFrames().add(kfY);

        return timeline;
    }

    private SequentialTransition animateMergedTile(Tile tile) {
        final ScaleTransition scale0 = new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale0.setToX(1.2);
        scale0.setToY(1.2);
        scale0.setInterpolator(Interpolator.EASE_IN);

        final ScaleTransition scale1 = new ScaleTransition(ANIMATION_MERGED_TILE, tile);
        scale1.setToX(1.0);
        scale1.setToY(1.0);
        scale1.setInterpolator(Interpolator.EASE_OUT);

        return new SequentialTransition(scale0, scale1);
    }

    public void move(Direction direction){
        if (!table.isLayerOn().get()) {
            moveTiles(direction);
        }
    }
    
    public void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    public BooleanProperty isLayerOn() {
        return table.isLayerOn();
    }
    public void pauseGame() {
        table.pauseGame();
    }
    public void quitGame() {
        table.quitGame();
    }
    public void saveSession() {
        table.saveSession();
    }

    private void doSaveSession() {
        table.saveSession(gameGrid);
    }
    public void restoreSession() {
        table.restoreSession();
    }
    private void doRestoreSession() {
        initializeGameGrid();
        if (table.restoreSession(gameGrid)) {
            redrawTilesInGameGrid();
        }
    }

    public void saveRecord() {
        table.saveRecord();
    }

    public void tryAgain() {
        table.tryAgain();
    }
    
    public void aboutGame() {
        table.aboutGame();
    }
    
    public void setToolBar(HBox toolbar){
        table.setToolBar(toolbar);
    }
    
    public void setHostServices(HostServices hostServices){
        table.setHostServices(hostServices);
    }
}

