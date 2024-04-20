package Tanks;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONObject;
import processing.core.PApplet;
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
    public static final int TOPBAR = 0;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;
    private Level testLevel; private Level testLevel2;; private Level testLevel3;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 30;
    private int i = 0;

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
        for(int i = 0; i < levels.length(); i++){
            Level newLevel = new Level(this, "config.json", initialLevel++);
            allLevels.add(newLevel);
            allTerrains.add(newLevel.initialiseEntities());
        }
        //See PApplet javadoc:
		//loadJSONObject(configPath)
		//loadImage(this.getClass().getResource(filename).getPath().toLowerCase(Locale.ROOT).replace("%20", " "));

    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        
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
    int index = 0;
	@Override
    public void draw() {
        if(!allLevels.get(index).isDoneLevel()){
            allTerrains.get(index).renderTerrain();
        }
        else{
            index++;
        }

        //----------------------------------
        //display HUD:
        //----------------------------------
        //TODO

        //----------------------------------
        //display scoreboard:
        //----------------------------------
        //TODO
        
		//----------------------------------
        //----------------------------------

        //TODO: Check user action
    }


    public static void main(String[] args) {

        PApplet.main("Tanks.App");
    }

}
