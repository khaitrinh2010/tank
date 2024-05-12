package Tanks;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONObject;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class App extends PApplet {
    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    int index = 0;
    public static final int TOPBAR = 0;
    private int start = 0;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;
    private ArrayList<Tank> tankArray;
    private ArrayList<Tank> tanksList = new ArrayList<>();
    private int transitionTime = 0;
    private int endTime = 700;
    private Level testLevel; private Level testLevel2;; private Level testLevel3;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 30;
    float initialPoint = 190;
    private int startTime = 0;
    private int len;
    public String configPath;
    ArrayList<Level> allLevels = new ArrayList<>();
    ArrayList<Terrain> allTerrains = new ArrayList<>();

    public static Random random = new Random();
	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        index  = 0;
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(this.configPath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Parse the content into a JSON object;
        org.json.JSONObject jsonObject = new JSONObject(content);
        JSONObject parsedJSON = jsonObject;
        //The levels array in the config.json file
        JSONArray levels = jsonObject.getJSONArray("levels");
        int initialLevel = 1;
        Level prev = null;
        for(int i = 0; i < levels.length(); i++){
            Level newLevel = null;
            if(i == 0) {
                newLevel = new Level(this, this.configPath, initialLevel++, null);
            }
            else{
                //If the current level is > level 1
                newLevel = new Level(this, this.configPath, initialLevel++, prev);
            }
            prev = newLevel;
            allLevels.add(newLevel);
            Terrain newTerrain = newLevel.initialiseEntities();
            allTerrains.add(newTerrain);
            len = allLevels.size();
        }
        for(Level level: allLevels){
            level.setLevels(allLevels);
        }

    }

    public Level getCurrentLevel(){
        return allLevels.get(index);
    }


    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        for (Tank tank : new ArrayList<>(allLevels.get(index).getTanksList())) {
            if (tank.isThisPlayerTurn() && allTerrains.get(index).getAverageHeight()[tank.getX()] <= tank.getY()) {
                if(event.getKeyCode() == 38 || event.getKeyCode() == 40){
                    tank.updateTurret(event.getKeyCode());
                }
                if(event.getKeyCode() == PConstants.RIGHT || event.getKeyCode() == PConstants.LEFT){
                    tank.updateMovement(event.getKeyCode());
                }
                if(event.getKey() == 'w' || event.getKey() == 's'){
                    tank.updatePower(event.getKey());
                }
                if(event.getKey() == 'f' || event.getKey() == 'r' && !allLevels.get(index).isGameOver()){
                    tank.powerUps(event.getKey());
                }
                if(event.getKey() == ' '){
                    tank.fireProjectile(event.getKey());
                }
                if(event.getKey() == 'r' && allLevels.get(index).isGameOver()){
                    allLevels.clear();
                    allTerrains.clear();

                    this.setup();
                }
            }
        }
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){

    }

    @Override
    public void mousePressed(MouseEvent e) {
        //TODO - powerups, like repair and extra fuel and teleport
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Draw all elements in the game by current frame.
     */

    int idx = 0;
	@Override
    public void draw() {
        if(index >= allLevels.size() - 1){
            index = allLevels.size() - 1;
        }
        if(index < 0){
            index = 0;
        }
        if(allLevels.isEmpty()){
            this.setup();
        }
        if (allLevels.get(index).isGameOver()) {
            Level currentLevel = allLevels.get(index);
            allTerrains.get(index).renderTerrain(this, currentLevel.getTreesList(), currentLevel.getTreesPosition(), currentLevel.getAssets(), currentLevel.getColorList());
        } else if (!allLevels.get(index).isDoneLevel()) {
            Level level = allLevels.get(index);
            allTerrains.get(index).renderTerrain(this, level.getTreesList(), level.getTreesPosition(), level.getAssets(), level.getColorList());
        } else if (allLevels.get(index).isDoneLevel() && !allLevels.get(index).isGameOver()) {
            index++;
            if (index < allLevels.size()) {
                Level level = allLevels.get(index);
                level.updateTankList();
                Terrain currentTerrain = allTerrains.get(index);
                currentTerrain.setTanks(level.getTanksList());
                ;
                currentTerrain.setState(level.getState());
                ;
            }
    }
    }
    public void setConfigPath(String newConfigPath){
        this.configPath = newConfigPath;
    }

    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }
}
