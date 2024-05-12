package Tanks;

import org.json.JSONObject;
import processing.core.PApplet;
import processing.core.PImage;
import java.util.*;

import static java.lang.Math.abs;
/* NOTE THAT:
 * x is the areaa under X
 * X is the terrain height
 * A,B,C,D,E: denotes the player
 * 0,1,2,3,.. denotes machine player
 * @ denotes empty space (the sky that aboves the X)
 * */

public class Terrain {
    private PApplet sketch;
    public GameState gameState;
    private HashSet<Explosion> destruction;
    float[] averageHeight = new float[896]; //each pixel
    public ArrayList<Tank> tanksList;
    //Max 28 trees;
    private boolean[] hasDead;
    private boolean[] hasUpdated;
    ArrayList<Explosion> handleDraw;
    Level currentLevel;
    /**
     * Constructs a Terrain object with a specified game state, layout setting, height data, list of tanks, and the current level.
     *
     * @param gameState The game state associated with this terrain.
     * @param averageHeight An array of float representing the height of terrain at each horizontal pixel.
     * @param tanksList A list of tanks currently active on this terrain.
     * @param level The current level object associated with this terrain.
     */
    public Terrain (GameState gameState, float[] averageHeight, ArrayList<Tank> tanksList, Level level){
        this.gameState = gameState;
        this.averageHeight = averageHeight;
        this.tanksList = tanksList;
        this.destruction = new HashSet<>();
        currentLevel = level;
        hasDead = new boolean[this.tanksList.size()];
        hasUpdated = new boolean[this.tanksList.size()];
        Arrays.fill(hasDead, false);
        handleDraw = new ArrayList<>();
    }

    /**
     * Returns the average height data of this terrain
     * @return  a float array represents the height at each point in this terrain
     */
    public float[] getAverageHeight(){
        return this.averageHeight;
    }

    /**
     * Calculates the impact of a explosion in the terrain
     * @param x, x coordinate of the terrain
     * @param y, y coordinate of terrain
     * @param centerX, x coordinate of the explosion
     * @param centerY, y coordinate of explosion
     * @return teh distance from the center of the explosion
     */
    public double calculateImpactRange(float x, float y, float centerX, float centerY){
        return (Math.pow(centerX-x,2) + Math.pow(centerY-y,2));
    }

    /**
     * Update the tank's properties based on the destruction in the terrain
     * @param destruction, a set of destruction's to handle
     */
    public void updateTank(HashSet<Explosion> destruction){
        for(Explosion explosion: destruction) {
            int centerX = explosion.getX();
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
                if(trueDistance <= radius && tank.getHealth() > 0){
                    damage = (1 - trueDistance/radius)*60;
                    tank.setHealth(tank.getHealth() - (int) damage);
                    if(tank != tankCausation) {
                        tankCausation.setPoint(tankCausation.getPoint() + (int) damage);
                    }
                }
            }
        }
    }

    /**
     * Update the terrain's height at coordinates that are affected by the explosion in the terrain
     * @param destruction,  a set of destruction's to handle
     */
    public void updateTerrain(HashSet<Explosion> destruction){
        for(Explosion e: destruction){
            int centerX = (int) e.getX();
            float centerY = e.getY();
            float radius = e.getRadius();
            int start = Math.max(0, (int)  (centerX - radius));
            int end = Math.min(864, (int) (centerX + radius));
            for(int i = start; i < end; i++) {
                float distance = (float) Math.sqrt(Math.pow(radius, 2) - Math.pow(centerX - i, 2));
                if (this.averageHeight[i] < centerY - distance){
                    this.averageHeight[i] += 2 * distance;
                }
                else if (this.averageHeight[i] >= centerY - distance && this.averageHeight[i] <= centerY + distance) {
                    this.averageHeight[i] = centerY + distance;
                }

            }
        }
    }

    /**
     * draw any explosion that has not completed the animation in the terrain
     */
    public void drawExplosion(){
        Iterator<Explosion> it = handleDraw.iterator();
        while(it.hasNext()){
            Explosion nextExplosion = it.next();
            if(!nextExplosion.isHasDoneExplode()){
                nextExplosion.drawExplosion();
            }
            else{
                it.remove();
            }
        }
    }

    /**
     * Set the height of the tank to new height if the terrain is destroyed, also check whether the tank has been deduced health by falling
     */
    public void updateTankHeight(){
        ArrayList<Explosion> destruct = new ArrayList<>(this.destruction);
        Explosion explosion = null;
        Tank tankCause = null;
        if(!destruct.isEmpty()){
            explosion  = destruct.get(0);
            tankCause = explosion.getTank();
        }
        for(Tank tank: this.tanksList){
            int xTank = tank.getX();
            float newY = this.averageHeight[xTank];
            if(newY != tank.getY()) {
                int damage = (int) (newY - tank.getY());
                if((tank.getParachuteCount() == 0 || tank.getY() >= 640) && !hasUpdated[this.tanksList.indexOf(tank)]) {
                    hasUpdated[this.tanksList.indexOf(tank)] = true;
                    if (tankCause != null && tankCause != tank && (tank.getHealth() > 0) && tank.getX() <= explosion.getX() + explosion.getRadius() && tank.getX() >= explosion.getX() - explosion.getRadius()){
                        tankCause.setPoint(tankCause.getPoint() + damage);
                    }
                    tank.setHealth(tank.getHealth() - damage);
                }
                tank.reduceY(1.0f / sketch.frameRate, newY);
            }
            else{
                hasUpdated[this.tanksList.indexOf(tank)] = false;
            }
        }
        for(Tank tank: this.tanksList){
            tank.setAverageHeight(this.averageHeight);
        }
        this.destruction.clear();
    }

    /**
     * Set the game state associated with this terrain
     * @param newState, a GameState object.
     */
    public void setState(GameState newState){
        this.gameState = newState;
    }

    /**
     * Get the tanks that are displayed on this terrain
     * @return an ArrayList of Tank object
     */
    public ArrayList<Tank> getTankList(){
        return this.tanksList;
    }

    /**
     * adjust the height of the terrain at any specific point
     * @param index, the x coordinate want to be changed
     * @param newY, the new y coordinate at the point.
     */
    public void setTerrain(int index, float newY){
        this.averageHeight[index] = newY;
    }

    /**
     * When a level is transited, update the tanks active in this terrain.
     * @param tanks, a new ArrayList of Tank object.
     */
    public void setTanks(ArrayList<Tank> tanks){
        this.tanksList = tanks;
    }

    /**
     * Renders the terrain including tanks, explosions, and other elements.
     *
     * @param sketch The PApplet used for drawing.
     * @param treesList A list of tree coordinates and modifications.
     * @param treesPosition A boolean array indicating the presence of trees.
     * @param assets A list of PImages used as visual assets.
     * @param colorList An array of color values for drawing.
     */
    public void renderTerrain(PApplet sketch, ArrayList<float[]> treesList, boolean[] treesPosition, ArrayList<PImage> assets, int[] colorList){
            sketch.image(assets.get(0), 0, 0);
            if(hasUpdated.length != this.tanksList.size()){
                hasUpdated = new boolean[this.tanksList.size()];
                Arrays.fill(hasUpdated, false);
            }
            //DRAW THE MAP
            drawExplosion();
            updateTerrain(this.destruction);
            updateTank(this.destruction);
            this.updateTankHeight();
            for (int i = 0; i < this.averageHeight.length; i++) {
                sketch.stroke(colorList[0], colorList[1], colorList[2]);
                sketch.rect(i, this.averageHeight[i], 1, 640 - this.averageHeight[i]);
            }
            for (int i = 0; i < treesList.size(); i++) {
                int x = (int) treesList.get(i)[0]; // toa do x
                float randomNum = treesList.get(i)[2];
                int imageHeight = assets.get(1).height;
                if (treesPosition[x]) { //which denotes there's a tree there
                    int xPos = (int) (x*32 + randomNum);
                    if(xPos >= 896){
                        xPos = 895;
                    }
                    else if(xPos < 0){
                        xPos = 0;
                    }
                    float yPos = (int) Math.min(895, this.averageHeight[Math.min(895, xPos + 16)] - imageHeight);
                    sketch.image(assets.get(1), xPos, yPos);
                }
            }
            this.gameState.drawScoreBoard();
            if(this.currentLevel.isGameOver()){
                return;
            }

            for (int i = 0; i < this.tanksList.size(); i++){
                Tank tankToDraw = this.tanksList.get(i);
                if(hasDead.length == this.tanksList.size()) {
                    if (!hasDead[i] && (tankToDraw.getHealth() <= 0 || tankToDraw.getY() >= 640) && this.averageHeight[tankToDraw.getX()] <= tankToDraw.getY()) {
                        if(tankToDraw.getHealth() <= 0) {
                            handleDraw.add(new Explosion(tankToDraw, sketch, tankToDraw.getX(), tankToDraw.getY(), 15, null));
                        }
                        else if(tankToDraw.getY() >= 640){
                            handleDraw.add(new Explosion(tankToDraw, sketch, tankToDraw.getX(), tankToDraw.getY(), 30, null));

                        }
                        hasDead[i] = true;
                        tankToDraw.setHealth(0);
                    }
                }
                else{
                    hasDead = new boolean[this.tanksList.size()];

                }
                if (!tankToDraw.getTankExplosionList().isEmpty()) {
                    Explosion firstExplosion = tankToDraw.getTankExplosionList().get(0);
                    this.destruction.addAll(tankToDraw.getTankExplosionList());
                    handleDraw.addAll(tankToDraw.getTankExplosionList());
                    tankToDraw.getTankExplosionList().remove(firstExplosion);
                }
                tankToDraw.drawTank(sketch);
                tankToDraw.endTurn();
            }
    }
}
