package Tanks;
import org.json.JSONObject;
import org.json.JSONArray;
import processing.core.PApplet;
import processing.core.PImage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//This will setup all the information about the current level of the game

public class Level {
    private int levelNumber;
    private Level prevLevel;
    ArrayList<PImage> assets = new ArrayList<>();
    ArrayList<PImage> assets2 = new ArrayList<>();
    float[] averageHeight = new float[896]; //each pixel
    boolean[] treesPosition = new boolean[28]; //32 pixels each
    private PApplet sketch;
    // a json object in the config.json file represent the currentLevel object
    private JSONObject currentLevel;
    private boolean isDoneLevel = false;
    private String configFilePath;
    private JSONObject parsedJSON;
    private ArrayList<Tank> tanksList = new ArrayList<>();
    private ArrayList<float[]> treesList = new ArrayList<>();
    private GameState currentState;
    float[] randomList = new float[28];
    ArrayList<Level> allLevels;
    int[] colorList = new int[3];
    private Level prevlevel;
    private int startTime = 0;

    /**
     *
     * @param sketch PApplet instance for drawing
     * @param configFilePath the config file
     * @param levelNumber an integer represent the level number
     * @param prevLevel Level object represents the last level
     */
    public Level(PApplet sketch, String configFilePath, int levelNumber, Level prevLevel){
        this.sketch = sketch;
        this.configFilePath = configFilePath;
        this.levelNumber = levelNumber;
        this.currentLevel = parseJSONFile();
        this.prevLevel = prevLevel;
        randomNum();
        extractTXTFile();
    }

    /**
     * Parse the information for the current level
     * @return a JSON object represents this level's configuration
     */
    public JSONObject parseJSONFile(){
        try{
            //Read the content of the JSON File
            String content = new String(Files.readAllBytes(Paths.get(this.configFilePath)));
            //Parse the content into a JSON object;
            JSONObject jsonObject = new JSONObject(content);
            this.parsedJSON = jsonObject;
            //The levels array in the config.json file
            JSONArray levels = jsonObject.getJSONArray("levels");
            this.currentLevel = levels.getJSONObject(this.levelNumber - 1);
            return this.currentLevel;
        }
        catch(Exception e){
            return new JSONObject();
        }
    }

    /**
     * Generate an array or random numbers for the tree's location in the terrain
     * @return a float[] array of random numbers
     */
    public float[] randomNum(){
        for(int i = 0; i < randomList.length; i++){
            randomList[i] = this.sketch.random(-30, 30);
        }
        return randomList;
    }

    /**
     * Retrieves a list of coordinate of the trees in this level (x,y,random number)
     * @return an ArrayList of float arrays represent the information about the tree.
     */
    public ArrayList<float[]> getTreesList(){
        return treesList;
    }
    /**
     * Retrieves an array indicates the position of the tree
     * @return an boolean array where true indicates there's a tree at that position in the 896-length array
     */
    public boolean[] getTreesPosition(){
        return treesPosition;
    }

    /**
     * Set the list of levels of the game, allow navigating between levels
     * @param levels, an ArrayList of Level object representing all levels in the game
     */
    public void setLevels(ArrayList<Level> levels){
        allLevels = levels;
    }

    /**
     * Help smooth the 896-size array represents the height of the terrain at specific point
     * @param index, index to compute the new value associated with that index
     * @param heights, Array of height across the terrain
     * @return a float represent the new value at that index
     */
    public float computeAverage(int index, float[] heights){
        //Because the heights array  is 28 max length
        float sum = 0;
        float endPoint = index + 32;

        //The window is 864 x 640
        if(endPoint > 896){
            endPoint = 896;
        }
        float count = 0;
        for(int i = index; i < endPoint; i++){
            sum += heights[i];
            count++;
        }
        return sum/count;
    }

    /**
     * Extract and process the terrain and object replacement from a layout file.
     */
    public void extractTXTFile(){
        String fileToBeParsed = this.currentLevel.getString("layout");
        String[] components = this.sketch.loadStrings(fileToBeParsed);
        float[] heights = new float[28];
        //The heights array contain height of the 28 X points
        //Draw the map
        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 28; j++){
                try {
                    char element = components[i].charAt(j);
                    if(element == 'X'){
                        heights[j] = i*32;
                    }
                    else if(element == 'T'){
                        treesPosition[j] = true;
                        float randomNum  = randomList[j];
                        treesList.add(new float[]{j, i, randomNum});
                    }
                }
                catch(Exception e){
                }
            }
        }
        //Setup the rest
        float[] firstPass = new float[896];
        float[] secondPass = new float[896];

        for(int i = 0; i < 28; i++){
            for(int j = i*32; j < (i + 1)*32; j++){
                averageHeight[j] = heights[i];
            }
        }
        for(int  i = 0; i < averageHeight.length; i++){
            firstPass[i] = computeAverage(i, averageHeight);
        }
        for(int  i = 0; i < averageHeight.length; i++){
            secondPass[i] = computeAverage(i, firstPass);
        }
        averageHeight = secondPass;
        //Images
        if(this.levelNumber == 1){
            for(int i = 0; i < 28; i++) {
                for (int j = 0; j < 20; j++) {
                    try {
                        char element = components[j].charAt(i);
                        String elem = String.valueOf(element);
                        if (element != 'X' && element != 'T' && element != ' ') {
                            Tank newTank = new Tank(this.parsedJSON, this.sketch, elem, i * 32, averageHeight[i * 32], averageHeight, this.currentState);
                            this.tanksList.add(newTank);
                        }
                    }
                    catch (Exception e) {
                    }
                }
            }

        }
        this.currentState = new GameState(this, this.sketch);
        this.currentState.setTankList(this.tanksList);
        for(Tank tank: this.tanksList){
            tank.setState(this.currentState);
        }
        String assetsDir = System.getProperty("user.dir") + "/src/main/resources/Tanks/";
        String backgroundImage = assetsDir + this.currentLevel.getString("background");
        String foregroundColor = this.currentLevel.getString("foreground-colour");

        colorList[0] = Integer.parseInt(foregroundColor.split(",")[0]);
        colorList[1] = Integer.parseInt(foregroundColor.split(",")[1]);
        colorList[2] = Integer.parseInt(foregroundColor.split(",")[2]);
        PImage treeImage = null;
        if(this.currentLevel.has("trees")){
            String treesImagePath = assetsDir + this.currentLevel.getString("trees");
            treeImage = this.sketch.loadImage(treesImagePath);
            treeImage.resize(32, 32);
        };

        PImage background = this.sketch.loadImage(backgroundImage);
        assets.add(background);
        assets.add(treeImage);
        String fuelpath = assetsDir + "fuel.png";
        String parachutePath = assetsDir + "parachute.png";
        String windPath = assetsDir + "wind.png";
        String windPath2 = assetsDir + "wind-1.png";
        String firePath =  assetsDir + "fire-removebg-preview.png";
        assets2.add(this.sketch.loadImage(fuelpath));
        assets2.add(this.sketch.loadImage(parachutePath));
        assets2.add(this.sketch.loadImage(windPath));
        assets2.add(this.sketch.loadImage(windPath2));
        assets2.add(this.sketch.loadImage(firePath));
        this.currentState.setAssets(assets2);
    }

    /**
     * Retrives which state the game is on, each level has its own state
     * @return a GameState object associated with this level
     */
    public GameState getState(){
        return this.currentState;
    }

    /**
     * After extract and process the  information for this level, save the assets used for this level
     * in a file to draw
     * @return an ArrayList of PImage object represents all images needed for this level
     */
    public ArrayList<PImage> getAssets(){
        return assets;
    }

    /**
     * Get the color list for the terrain associated with this level
     * @return an array list of intger represents the r g b value
     */
    public int[] getColorList(){
        return colorList;
    }

    /**
     * Check if this level is done to move to the next level
     * @return true if this level is done, false otherwise
     */
    public boolean isDoneLevel(){
        if(this.currentState.isGameOver()){
            isDoneLevel = true;
        }
        return isDoneLevel;
    }

    /**
     * Sets the completion status of this level
     * @param value, boolean value determines whether this level is done or not
     */
    public void setDoneLevel(boolean value){
        isDoneLevel = value;
    }

    /**
     * Fromt the information parsed, initialise the terrain associated with this level
     * The terrain object then can handle the development of the current level
     * @return a Terrain object
     */
    public Terrain initialiseEntities(){
        parseJSONFile();
        Terrain terrainObject = new Terrain(this.currentState, averageHeight, this.tanksList, this);
        return terrainObject;
    }

    /**
     * Get the tanks that participate in this level
     * @return a list includes all tanks in this level
     */
    public ArrayList<Tank> getTanksList(){
        return this.tanksList;
    }

    /**
     * Return the integer represents the level number(1,2,3)
     * @return an integer associated with this level
     */
    public int getLevelNumber(){
        return this.levelNumber;
    }

    /**
     * Check if the game is over
     * @return true if the ggame is over, false otherwise
     */
    public boolean isGameOver(){
        if(this.levelNumber == allLevels.size() && this.isDoneLevel()){
            return true;
        }
        return false;
    }

    /**
     * Returns the height arrays that being processed by the previous functions
     * @return averageHeight, a float[] array represents the height of the terrain associated with this level
     */
    public float[] getAverageHeight(){
        return averageHeight;
    }

    /**
     * Set the list of tanks participate in this level
     * @param tanks, an ArrayList of Tank object
     */
    public void setTanksList(ArrayList<Tank> tanks){
        this.tanksList = tanks;
    }

    /**
     * Gets called everytime a level is done, the current level tank list will be passed to the next level
     * Update the new level's tank's list, set new information for the tank(x, y, game state, averageHeight) and reset the state of the tank
     */
    public void updateTankList(){
        if(this.levelNumber > 1) {
            this.setTanksList(this.prevLevel.getTanksList());
            for (Tank tank : this.tanksList) {
                tank.resetState();
            }
        }
        String fileToBeParsed = this.currentLevel.getString("layout");
        String[] components = this.sketch.loadStrings(fileToBeParsed);
        //if(this.levelNumber > 1) {
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 20; j++) {
                try {
                    char element = components[j].charAt(i);
                    String elem = String.valueOf(element);
                    if (element != 'X' && element != 'T') {
                        for(Tank tank: this.tanksList){
                            tank.setState(this.currentState);
                            if(tank.getPlayerNumber().equals(elem)){
                                tank.setX(i*32);
                                tank.setY(averageHeight[i*32]);
                                tank.setAverageHeight(averageHeight);;
                            }
                        }
                    }
                } catch (Exception e){

                }
            }
            //}
        }
        this.currentState.setTankList(this.tanksList);
    }
}
    