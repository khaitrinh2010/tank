package Tanks;
import org.json.JSONObject;
import org.json.JSONArray;
import processing.core.PApplet;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//This will setup all the information about the current level of the game

public class Level {
    private int levelNumber;
    float[] averageHeight = new float[896]; //each pixel
    boolean[] treesPosition = new boolean[28]; //32 pixels each
    private PApplet sketch;
    // a json object in the config.json file represent the currentLevel object
    private JSONObject currentLevel;
    private String configFilePath;
    private JSONObject parsedJSON;
    private ArrayList<Tank> tanksList = new ArrayList<>();
    private ArrayList<float[]> treesList = new ArrayList<>();
    private ArrayList<float[]> tanksPosition = new ArrayList<>();
    private ArrayList<String> tanksNumber = new ArrayList<>();
    private GameState currentState;
    float[] randomList = new float[28];
    HashMap<Integer, String> mappingCurrentPlayer = new HashMap<>();

    //constructor of the class
    public Level(PApplet sketch, String configFilePath, int levelNumber){
        this.sketch = sketch;
        this.configFilePath = configFilePath;
        this.levelNumber = levelNumber;
        //JSON object represent the curerent leevel in the levels array
        this.currentLevel = parseJSONFile();
        randomNum();
        extractTXTFile();

        //The entire JSON object
    }

    /* PARSE THE JSON FILE*/
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
    public void randomNum(){
        for(int i = 0; i < randomList.length; i++){
            randomList[i] = this.sketch.random(-30, 30);
        }
    }
    public float computeAverage(int index, float[] heights){
        //Because the heights array  is 28 max length
        int sum = 0;
        float endPoint = index + 32;

        //The window is 864 x 640
        if(endPoint > 896){
            endPoint = 896;
        }
        float count = 32;
        for(int i = index; i < endPoint; i++){
            sum += heights[i];
        }
        return sum/count;

    }
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
                        //each pixel is 32 value
                        heights[j] = i*32; //heights by pixel
                    }
                    else if(element == 'T'){
                        treesPosition[j] = true;
                        float randomNum  = randomList[j]; //in 30 pixels
                        treesList.add(new float[]{j, i, randomNum}); // row, col
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
        for(int i = 864; i < 896; i++){
            averageHeight[i] = 480;
        }
        for(int  i = 0; i < averageHeight.length; i++){
            firstPass[i] = computeAverage(i, averageHeight);
        }
        for(int  i = 0; i < averageHeight.length; i++){
            secondPass[i] = computeAverage(i, firstPass);
        }


        averageHeight = secondPass;
        //Process the tank
        for(int i = 0; i < 28; i++){
            for (int j = 0; j < 20; j ++){
                try {
                    char element = components[j].charAt(i);
                    String elem = String.valueOf(element);
                    if ((Character.isAlphabetic(element) || Character.isDigit(element)) && element != 'X' && element != 'T') {
                        //so there will be a new tank right here
                        tanksPosition.add(new float[]{i, j});
                        tanksNumber.add(elem);
                    }
                }
                catch(Exception e){
                }

            }
        }
        int index = 0;
        for(int i = 0; i < tanksNumber.size(); i++){
            mappingCurrentPlayer.put(index++, tanksNumber.get(i));
        }

        this.currentState = new GameState(this.sketch, tanksNumber);
        for(int i = 0; i < 28; i++){
            for (int j = 0; j < 20; j ++){
                try {
                    char element = components[j].charAt(i);
                    String elem = String.valueOf(element);
                    if (element != 'X' && element != 'T') {
                        //so there will be a new tank right here
                        Tank newTank = new Tank(this.parsedJSON, this.sketch, elem, i * 32, averageHeight[i * 32], averageHeight, this.currentState);
                        this.currentState.tankList.add(newTank);
                        tanksList.add(newTank);
                    }
                }
                catch(Exception e){
                }

            }
        }

    }
    public boolean isDoneLevel(){
        return this.currentState.isGameOver();
    }
    public Terrain initialiseEntities(){
        parseJSONFile();
        //Note that this.currentLevel is a JSON object
        Thing newEntity = new Thing(this.parsedJSON, this.sketch);
        Terrain terrainObject = new Terrain(this.parsedJSON, this.sketch, this.currentState, this.currentLevel, averageHeight, tanksList, treesList, treesPosition, randomList);
        return terrainObject;
    }
}
