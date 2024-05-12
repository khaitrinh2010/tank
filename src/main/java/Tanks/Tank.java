package Tanks;

import org.json.JSONObject;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.util.*;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Tank extends Thing implements Movement, Comparable<Tank>{
    private String playerNumber;
    private int parachuteCount;
    public PApplet sketch;
    private String colorsOfTank;
    private float angle;
    private boolean decreaseParachute = false;
    private int arrowTime = 2000;
    private int startTurnTime = 0;
    public float[] averageHeight = new float[896];
    public GameState currentState;
    private boolean isDead;
    private int fuel;
    private int health;
    private float power;
    private String currentWorkingDirec = System.getProperty("user.dir");
    private ArrayList<Projectile> projectile;
    private boolean hasFired = false;
    private float turretX;
    private float turretY;
    int j = 1;
    private ArrayList<Explosion> tankExplosion;
    private int point;
    private PImage parachute;
    private boolean hasTurnedOff = false;
    private int ultiLevel;
    private String fireImagePath =  currentWorkingDirec + "/src/main/resources/Tanks/fire-removebg-preview.png";
    private PImage fireImage;
    private int[] listOfColor = new int[3];
    public Tank(JSONObject setting, PApplet sketch, String playerNumber, int x, float y, float[] averageHeight, GameState currentGameState){
        super(x , y);
        this.sketch = sketch;
        this.playerNumber = playerNumber;
        this.projectile = new ArrayList<>();
        this.fuel = 250;
        this.health = 100;
        this.power = 50;
        this.tankExplosion = new ArrayList<>();
        this.point = 0;
        this.ultiLevel = 0;
        this.parachuteCount = 3;
        loadOtherThings(currentGameState, setting);
    }
    /**
     * load various settings for the tank
     * @param currentGameState the current state of the game
     * @param setting JSONObject contains additional feature of the tank
    */
    public void loadOtherThings(GameState currentGameState, JSONObject setting){
        fireImage = this.sketch.loadImage(fireImagePath);
        fireImage.resize(70,70);
        String parachutePath = currentWorkingDirec + "/src/main/resources/Tanks/parachute.png";
        parachute = this.sketch.loadImage(parachutePath);
        parachute.resize(42, 42);
        averageHeight = averageHeight;
        currentState = currentGameState;
        String statement = setting.getJSONObject("player_colours").getString(playerNumber);
        if(!statement.equals("random")){
            listOfColor[0] = Integer.parseInt(statement.split(",")[0]);
            listOfColor[1] = Integer.parseInt(statement.split(",")[1]);
            listOfColor[2] = Integer.parseInt(statement.split(",")[2]);
        }

        else{
            Random random = new Random();
            listOfColor[0] = random.nextInt(256);
            listOfColor[1] = random.nextInt(256);
            listOfColor[2] = random.nextInt(256);
        }
        angle = 0;
    }
    /**
     * Return a list of explosion caused by the tank
     * @return ArrayList of Explosion Object
     */
    public ArrayList<Explosion> getTankExplosionList(){
        if(this.health <= 0){
            this.tankExplosion.clear();
        }
        return this.tankExplosion;
    }
    /**
     * Compares this tank to another tank by the letter represent the current player
     * @return negative integer or positive integer
     */
    public int compareTo(Tank otherTank){
        return this.playerNumber.compareTo(otherTank.getPlayerNumber());
    }
    /**
     * Returns the letter represent the player number
     * @return String represent the player number
     */
    public String getPlayerNumber(){
        return this.playerNumber;
    }

    /**
     * Deploys a parachute to the tank if the tank is falling
     * @param sketch the PApplet instance for drawing
     */
    public void renderParachute(PApplet sketch){
        sketch.image(parachute, this.x - 20, this.y-35);
    }
    /**
     * Draws the tank and its component elements
     * @param sketch the PApplet instance for drawing
     */
    public void drawTank(PApplet sketch){
        if(!hasTurnedOff){
            if(this.health <= 0 && this.isThisPlayerTurn() && this.y >= averageHeight[(int) this.x]){
                this.currentState.nextTurn();
                hasTurnedOff = true;
            }
        }

        if(this.health > 0 || !isDead){
            if(this.health <= 0 && this.averageHeight[this.x] <= this.y){
                isDead = true;
            }
            int lowerWidth = 24;
            float upperWidth = lowerWidth * 0.6f;
            int lowerHeight = 12;
            float upperHeight = lowerHeight / 2;
            if(this.ultiLevel == 3){
                sketch.image(fireImage, this.x - 35, this.y - 60);
            }
            sketch.fill(listOfColor[0], listOfColor[1], listOfColor[2]);
            sketch.rect(this.x - lowerWidth / 2, this.y - lowerHeight / 2, lowerWidth, lowerHeight, 40); //corner radius = 5
            sketch.rect(this.x - lowerWidth / 2 + (lowerWidth - upperWidth) / 2, this.y - lowerHeight / 2 - upperHeight, upperWidth, upperHeight, 40);
            //isolate the turret to only draw it
            sketch.pushMatrix();
            sketch.translate(this.x, this.y - lowerHeight - upperHeight / 6);
            turretX = this.x;
            turretY = this.y - lowerHeight - upperHeight / 6;
            //draw the turret
            sketch.rotate(angle);
            sketch.stroke(100, 100, 100);
            sketch.strokeWeight(3);
            sketch.line(0, 0, 0, -15);
            sketch.noStroke();
            sketch.popMatrix();
            this.updateProjectile(sketch);
            if (this.isThisPlayerTurn()) {
                this.drawArrow(sketch);
                this.renderPower(sketch);
                this.renderFuel(sketch);
                this.renderHealthBar(sketch);
                this.drawTurn(sketch);
                //draw the parachute icon
                sketch.textSize(22);
                sketch.fill(0);
                sketch.text(String.valueOf(this.parachuteCount), 220, 90);
                sketch.noFill();
            }
        }
    }
    /**
     * Draws the arrow points to the tank each turn
     * @param sketch the PApplet instance for drawing
     */
    public void drawArrow(PApplet sketch){
        if(startTurnTime < arrowTime) {
            sketch.pushMatrix();
            sketch.translate(this.x, this.y - 110);
            sketch.stroke(0);
            sketch.strokeWeight(2);
            sketch.line(0, 0, 0, 50);
            float arrowheadLength = 10;
            float angle = -PConstants.PI / 3;
            float endX1 = (float) (arrowheadLength * cos(angle));
            float endY1 = (float) (arrowheadLength * sin(angle));
            float endX2 = (float) (-arrowheadLength * cos(angle));
            float endY2 = (float) (arrowheadLength * sin(angle));
            sketch.line(0, 50, endX1, 50 + endY1);
            sketch.line(0, 50, endX2, 50 + endY2);
            sketch.noStroke();
            sketch.popMatrix();
            startTurnTime += 500.0f/sketch.frameRate;
        }
    }

    /**
     * Returns a list of projectils fired by the tank
     * @return ArrayList of Projectile object
     */
    public ArrayList<Projectile> getProjectile(){
        return this.projectile;
    }
    /**
     * returns the angle of the tank's turret
     * @return float representing the angle in radians
     */
    public float getAngle(){
        return this.angle;
    }
    /**
     * Draws the line "player X's turn"
     * @param sketch the PApplet instance for drawing
     */
    public void drawTurn(PApplet sketch){
        sketch.textSize(20);
        sketch.fill(0);
        sketch.text(this.currentState.generateReport(), 20, 32);
    }
    /**
     * returns the current point of this tank
     * @return int represent the current point of this player
     */
    public int getPoint(){
        return this.point;
    }
    public void setPoint(int point){
        this.point = point;
        this.ultiLevel++;
    }
    /**
     * Check if it is this player's turn
     * @return true if it is this plaayer's turn, false otherwise
     */
    public boolean isThisPlayerTurn(){
        return this.playerNumber.equals(this.currentState.getCurrentPlayer());
    }
    /**
     * Draw the text represents the current fuel of this tank
     * @param sketch PApplet instance for drawing
     */
    public void renderFuel(PApplet sketch){
        String text = String.valueOf(this.fuel);
        sketch.textSize(20);
        sketch.fill(0);
        sketch.text(text, 210, 32);
        sketch.noFill();
    }
    /**
     * Draw the health's bar associated with this player's health
     * @param sketch PApplet instance for drawing
     */
    public void renderHealthBar(PApplet sketch){
        if(this.health <= 0){
            this.health = 0;
        }
        sketch.textSize(20);
        sketch.fill(0);
        sketch.text("Health: ", 375, 32);
        sketch.noFill();
        sketch.fill(listOfColor[0], listOfColor[1], listOfColor[2]);
        sketch.rect(450, 10, 180, 30);
        sketch.strokeWeight(5);
        sketch.stroke(100, 100, 100);
        sketch.rect(450, 10, 180*this.power/100, 30);
        sketch.noStroke();
        //The line
        sketch.strokeWeight(2);
        sketch.stroke(255, 0, 0);
        sketch.line(450 + 180*this.power/100, 5, 450 + 180*this.power/100, 40);
        sketch.noStroke();
        //remaining health;
        if(this.health < 100) {
            sketch.fill(255);
            sketch.stroke(0);
            sketch.strokeWeight(2);
            sketch.rect(450 + 180 * this.health / 100, 10, 630 - (450 + 180 * this.health / 100), 30);
            sketch.noStroke();
            sketch.noFill();
        }
        sketch.fill(0);
        sketch.textSize(24);
        sketch.text(String.valueOf(this.health), 650, 32);
        sketch.noFill();
    }
    /**
     * Draw the text represents the current power of this tank
     * @param sketch PApplet instance for drawing
     */
    public void renderPower(PApplet sketch){
        if(this.power > this.health){
            this.power = this.health;
        }
        if(this.power < 0){
            this.power = 0;
        }
        sketch.fill(0);
        sketch.textSize(22);
        sketch.text("Power: ", 375, 70);
        String powerTank = String.valueOf((int) this.power);
        sketch.text(powerTank,450, 70);
        sketch.noFill();
    }
    /**
     * Update power of this tank based on the user's input
     * @param key the character key pressed by user
     */
    public void updatePower(char key){
        float deltaTime = 1.0f/30;
        if(this.power > this.health){
            this.power = this.health;
        }
        if(this.power < 0){
            this.power = 0;
        }
        else{
            if(key == 'w'){
                this.power += 36*deltaTime;
            }
            if(key == 's'){
                this.power -= 36*deltaTime;
            }
        }
    }

    /**
     * set the ultilvel for this player to a new number
     * @param level int represents the new level
     */
    public void setUltilevel(int level){
        this.ultiLevel = level;
    }
    /**
     * set the parachutee count for this player to a new number
     * @param number int represents the new parachute count
     */
    public void setParachuteCount(int number){
        this.parachuteCount = number;
    }
    /**
     * Fires a pprojectile from this tank
     * @param key char represents the input from the user when fire a projectile
     */
    public void fireProjectile(char key) {
        if(key == ' ' && !hasFired) {
            if(this.ultiLevel == 3){
                for(int i = 0; i < 5; i++){
                    Projectile newProjectile = new Projectile(this, this.turretX, this.turretY, this.power * 9 / 100 + i*10*9/100, angle, averageHeight, true);
                    this.projectile.add(newProjectile);
                    j++;
                }
                this.ultiLevel = 0;
            }
            else{
                Projectile newProjectile = new Projectile(this, this.turretX, this.turretY, this.power * 9 / 100, angle, averageHeight, false);
                this.projectile.add(newProjectile);
            }
            hasFired = true;
        }
    }
    /**
     * Resets the state of the tank when get to new level
     */
    public void resetState(){
        this.health = 100;
        this.power = 50;
        this.ultiLevel = 0;
        this.fuel = 250;
        this.projectile.clear();
        this.tankExplosion.clear();
        hasTurnedOff = false;
        isDead = false;
    }
    /**
     * Returns the color in r g b of the tank
     * @return int[] Array of RGB values
     */
    public int[] getColourList(){
        return listOfColor;
    }
    /**
     * Keep track of the projectile to see whether it hit the terrain or other tanks
     * @param deltaTime time per frame to calculate
     * @param sketch PApplet instance for drawing
     */
    public void updateProjectile(PApplet sketch){
        int windPower = this.currentState.getWind();
        Iterator<Projectile> it = this.projectile.iterator();
        if(this.projectile != null && !this.projectile.isEmpty()){
            while(it.hasNext()){
                Projectile p = it.next();
                if(!p.hasDoneFired(sketch, averageHeight)){
                    p.fire(windPower);
                    p.renderProjectile(sketch, listOfColor);
                }
                else{
                    it.remove();
                }
            }
        }
    }
    /**
     * Set the current fuel of this tank to a new number
     * @param fuel int represents the new fuel's amount
     */
    public void setFuel(int fuel){
        this.fuel = fuel;
    }
    /**
     * Returns the current amount of fuel by this player
     * @return integer represents the current amount of fuel of this tank
     */
    public int getFuel(){
        return this.fuel;
    }
    /**
     * Buys health or fuel by the current point of this player depends on the input from user
     * @param key character represent the input from the user
     */
    public void powerUps(char key){
        if(key == 'r'){
            if(this.point > 20){
                this.point -= 20;
                this.health += 20;
                if(this.health > 100){
                    this.health = 100;
                }
            }
        }
        else if(key == 'f'){
            if(this.point > 10){
                this.point -= 10;
                this.fuel += 200;
            }
        }
    }
    /**
     * Move to another player turn, set the fire status to false and the drawing time for the arrow to 0
     */
    public void endTurn(){
        if(hasFired){
            this.currentState.nextTurn();
            hasFired = false;
            startTurnTime = 0;
        }
    }
    /**
     * Returns the current health of this player
     * @return integer represents the health of this player
     */
    public int getHealth(){
        return this.health;
    }
    /**
     * Set the health of this player to a new number
     * @param health intetger represent the new amount of health
     */
    public void setHealth(int health){
        this.health = health;
    }
    /**
     * Returns number of parachute left of this player
     * @return an integer represents the number of parachute left
     */
    public int getParachuteCount(){
        return this.parachuteCount;
    }
    /**
     * decides whether to deploy a parachute to this tank based on the parachute count left, the tanks falls to the neweY coordinate
     * @param deltaTime a float represents time per frame
     * @param newY the new Y coordinate of this tank
     */
    public void reduceY(float deltaTime, float newY){
        if(this.y < newY && this.parachuteCount > 0 && !isDead) {
            this.renderParachute(this.sketch);
            this.y += deltaTime * 60.0f;
            if(this.y >= newY){
                this.y = newY;
                this.parachuteCount -= 1;
            }
        }
        else if(this.parachuteCount == 0){
            this.y += deltaTime * 120.0f;
            if(this.y >= newY){
                this.y = newY;
            }
        }
    }
    /**
     * update the tank turret's angle based on the input from the user
     * @param keyCode the keyCode of the input
     */
    public void updateTurret(int keyCode){
        float speed = 3.0f; //3 radians per second
        if (keyCode == PConstants.UP) {
            if (this.angle > 1.57F) {
                this.angle = 1.57F;
            }
            this.angle += speed/30;
        } else if (keyCode == PConstants.DOWN) {
            if (this.angle < -1.57F) {
                this.angle = -1.57F;
            }
            this.angle -= speed/30;
        }

    }
    /**
     * Set the angle of this tank to a new number
     * @param angle a floar represents the new angle
     */
    public void setAngle(float angle){
        this.angle = angle;
    }
    /**
     * update the tank's position based on the input from the user
     * @param keyCode the keyCode of the input
     */
    public void updateMovement(int keyCode){
        if (keyCode == PConstants.RIGHT && this.fuel > 0) {
            this.goRight();
            this.fuel -= 1;
        }
        if (keyCode == PConstants.LEFT && this.fuel > 0) {
            this.goLeft();
            this.fuel -= 1;
        }

    }
    /**
     * set the average height of the terrain this tank is playing, ensure transition to new level smoothly
     * @param newAverage a float[] Array represents the new height of the terrain
     */
    public void setAverageHeight(float[] newAverage){
        averageHeight = newAverage;
    }
    /**
     * Update the posisition of the tank per frame when it goes left
     */
    @Override
    public void goLeft() {
        this.x -= 2;
        if(this.x < 0){
            this.x = 0;
        }

        this.y = averageHeight[this.x];
    }
    /**
     * update the state of the game where this tank is playing, for example update from level1 to level2
     * @param newState a GameState instane represents the new state of the game
     */
    public void setState(GameState newState){
        this.currentState = newState;
    }
    /**
     * Update the posisition of the tank per frame when it goes right
     */
    @Override
    public void goRight() {
        this.x += 2;

        if(this.x > 864){
            this.x = 864;
        }
        this.y = averageHeight[this.x];
    }
}
