package Tanks;

import org.json.JSONObject;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import java.util.*;

import java.util.Arrays;

import static com.google.common.primitives.Floats.min;
/* NOTE THAT:
 * x is the areaa under X
 * X is the terrain height
 * A,B,C,D,E: denotes the player
 * 0,1,2,3,.. denotes machine player
 * @ denotes empty space (the sky that aboves the X)
 * */

public class Terrain extends Thing {
    private JSONObject layoutSetting;
    private PApplet sketch;
    private GameState gameState;
    private boolean hasDoneUpdate = true;
    private String layoutPath;
    private PImage backgroundImage;
    private String foregroundColor;
    private PImage treesImage;
    private JSONObject parseJSON;
    private HashSet<Explosion> destruction;

    float[] averageHeight = new float[896]; //each pixel
    private ArrayList<Tank> tanksList;
    private ArrayList<float[]> treesList;
    boolean[] treesPosition = new boolean[28];
    //Max 28 trees;
    float[] randomList = new float[28];
    private String foreGroundColor;
    private int[] colorList = new int[3];
    String currentWorkingDir = System.getProperty("user.dir");

    public Terrain (JSONObject parseJSON, PApplet sketch, GameState gameState, JSONObject layoutSetting, float[] averageHeight, ArrayList<Tank> tanksList, ArrayList<float[]> treesList, boolean[] treesPosition, float[] randomList){
        super(parseJSON, sketch);
        this.gameState = gameState;
        this.parseJSON = parseJSON;
        this.sketch = sketch;
        this.layoutSetting = layoutSetting;
        this.averageHeight = averageHeight;
        this.tanksList = tanksList;
        this.treesList = treesList;
        this.treesPosition = treesPosition;
        this.randomList = randomList;
        this.destruction = new HashSet<>();
        loadImages();



    }
    private void loadImages(){
        this.layoutPath = this.layoutSetting.getString("layout");
        String backgroundImagePath = currentWorkingDir + "/src/main/resources/Tanks/" + this.layoutSetting.getString("background");
        this.backgroundImage = this.sketch.loadImage(backgroundImagePath);
        this.foregroundColor = this.layoutSetting.getString("foreground-colour");
        colorList[0] = Integer.parseInt(this.foregroundColor.split(",")[0]);
        colorList[1] = Integer.parseInt(this.foregroundColor.split(",")[1]);
        colorList[2] = Integer.parseInt(this.foregroundColor.split(",")[2]);
        if(this.layoutSetting.has("trees")){
            String treesImagePath = currentWorkingDir + "/src/main/resources/Tanks/" + this.layoutSetting.getString("trees");
            this.treesImage = this.sketch.loadImage(treesImagePath);
            this.treesImage.resize(30, 30);
        };
    }
    public double calculateImpactRange(float x, float y, float centerX, float centerY){
        return (Math.pow(centerX-x,2) + Math.pow(centerY-y,2));
    }
    public float getNewY(float centerX, float centerY, float x, float y, float radius){
        if(y < centerY - radius){
            return centerY - (float)Math.sqrt(Math.pow(radius, 2) - Math.pow(centerX - x, 2));
        }
        return centerY + (float)Math.sqrt(Math.pow(radius, 2) - Math.pow(centerX - x, 2));
    }
    public void updateTank(HashSet<Explosion> destruction){
        Iterator<Tank> it = this.tanksList.iterator();
        while(it.hasNext()){
            Tank nextTank = it.next();
            if(nextTank.getHealth() <= 0){
                this.gameState.deleteTank(nextTank);
                it.remove();
            }
        }
        for(Explosion explosion: destruction) {
            float centerX = explosion.getX();
            float centerY = explosion.getY();
            float radius = explosion.getRadius();
            Tank tankCausation = explosion.getTank();
            for (int i = 0; i < this.tanksList.size(); i++) {
                Tank tank = this.tanksList.get(i);
                int tankX = tank.getX();
                float tankY = tank.getY();
                double distance = calculateImpactRange(tankX, tankY, centerX, centerY);
                double trueDistance = Math.sqrt(distance);
                double damage = 0;
                if(trueDistance <= radius){
                    damage = (1 - trueDistance/radius)*60;
                    tank.setHealth((int) damage);
                    tankCausation.setPoint((int) damage);
                }
            }
        }
    }
    public void updateTerrain(HashSet<Explosion> destruction){
        for(Explosion e: destruction){
            float centerX = e.getX();
            float centerY = e.getY();
            float radius = e.getRadius();
            int start = (int) Math.max(0, centerX - radius);
            int end = (int) Math.min(864, centerX + radius);
            for(int i = start; i <= end; i++){
                if(this.averageHeight[i] <= centerY + radius){
                    this.averageHeight[i] = getNewY(centerX, centerY, i, this.averageHeight[i], radius);
                }
            }
            hasDoneUpdate = true;
        }

    }

    public void updateTankHeight(){
        ArrayList<Explosion> destruct = new ArrayList<>(this.destruction);
        Explosion explosion;
        Tank tankCause = null;
        if(!destruct.isEmpty()){
            explosion  = destruct.get(0);
            tankCause = explosion.getTank();
        }
        for(Tank tank: this.tanksList){
            int xTank = tank.getX();
            float newY = this.averageHeight[xTank];
            if(newY != tank.getY()) {
                //System.out.println("Tank " + tank.getPlayerNumber() + " is damaged by being fall of " + ((int) newY - tank.getY()));
                int damage = (int) (newY - tank.getY());
                if(tank.getParachuteCount() == 0) {
                    tank.reduceHealthByFall(damage);
                    if (tankCause != null) {
                        tankCause.increasePointByFallTerrain(damage);
                    }
                }
                tank.reduceY(1.0f / this.sketch.frameRate, newY);
            }
        }
        this.destruction.clear();
    }

    public void renderTerrain(){
        this.sketch.image(this.backgroundImage,0,0);
        //DRAW THE MAP
        if(!this.destruction.isEmpty()) {
            updateTank(this.destruction);
            updateTerrain(this.destruction);
        }
        updateTankHeight();
        for (int i = 0; i < this.averageHeight.length; i++) {
            this.sketch.stroke(colorList[0], colorList[1], colorList[2]);
            this.sketch.line(i, this.averageHeight[i], i, 640);
        }

        for(int i = 0; i < this.treesList.size();  i++){
            int x = (int) this.treesList.get(i)[0]; // toa do x
            float origY =  this.averageHeight[x*32 + 16];
            float randomNum = this.treesList.get(i)[2];
            int imageHeight = this.treesImage.height;
            float y = Math.max(origY - imageHeight, origY - imageHeight + randomNum);
            if(treesPosition[x]){ //which denotes there's a tree there
                this.sketch.image(this.treesImage, x*32, y + 2); //add a small number to ensure no floating trees
            }
        }
        this.gameState.drawScoreBoard();
        for(int i = 0; i < this.tanksList.size(); i++){
            Tank tankToDraw = this.tanksList.get(i);
            if(!tankToDraw.getTankExpllosionList().isEmpty()) {
                this.destruction.add(tankToDraw.getTankExpllosionList().get(0));
            }
            tankToDraw.drawTank();
            tankToDraw.endTurn();
        }
    }

}
