package Tanks;

import org.json.JSONObject;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.util.*;

public class Tank extends Thing implements Movement, Comparable<Tank>{
    private String playerNumber;
    private int parachuteCount;
    private boolean hasIncreasedPointByFallTerrain = false;
    private boolean hasBeenReducedByFall = false;
    private boolean hasIncreasedPoint = false;
    private boolean hasBeenReduced = false;
    private boolean hasDoneExplode = true;
    private PApplet sketch;
    private ArrayList<Explosion> explosionArrayList = new ArrayList<>();
    private String tankImage;
    private String colorsOfTank;
    private float angle;
    private boolean decreaseParachute = false;
    private int x;
    private float y;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private boolean endTurn = false;
    float[] averageHeight = new float[896];
    public GameState currentState;
    private boolean isDead;
    JSONObject setting;
    private ArrayList<String> mappingCurrentPlayer = new ArrayList<>();
    private int fuel = 250;
    private int health = 100;
    private float power = 50;
    private String currentWorkingDirec = System.getProperty("user.dir");
    private String fuelPath = currentWorkingDirec + "/src/main/resources/Tanks/fuel.png";
    private ArrayList<Projectile> projectile;
    private PImage fuelImage;
    private boolean hasFired = false;
    private float turretX;
    private float turretY;
    private ArrayList<Explosion> tankExplosion;
    private int point;
    public boolean doneParachute = false;
    private PImage parachute;
    private boolean isHit;
    public Tank(JSONObject setting, PApplet sketch, String playerNumber, int x, float y, float[] averageHeight, GameState currentGameState){
        super(setting, sketch);
        this.playerNumber = playerNumber;
        this.setting = setting;
        //This will contain the list of colour apply to this tank
        this.colorsOfTank = setting.getJSONObject("player_colours").getString(playerNumber);
        this.sketch = sketch;
        this.angle = 0; //initially
        this.averageHeight = averageHeight;
        this.currentState = currentGameState;
        //x, y coordinate
        this.x = x;
        this.y = y;
        this.fuelImage = this.sketch.loadImage(fuelPath);
        this.fuelImage.resize(30,30);
        this.projectile = new ArrayList<>();
        this.fuel = 250;
        this.health = 100;
        this.power = 50;
        this.tankExplosion = new ArrayList<>();
        this.point = 0;
        this.parachuteCount = 3; //Initially
        this.currentState.tankList.add(this);
        String parachutePath = currentWorkingDirec + "/src/main/resources/Tanks/parachute.png";
        parachute = this.sketch.loadImage(parachutePath);
        parachute.resize(42, 42);

    }
    public ArrayList<Explosion> getTankExpllosionList(){
        return this.tankExplosion;
    }
    public int compareTo(Tank otherTank){
        return this.playerNumber.compareTo(otherTank.getPlayerNumber());
    }
    public String getPlayerNumber(){
        return this.playerNumber;
    }
    public void renderParachute(){
        this.sketch.image(parachute, this.x - 20, this.y-35);
    }

    public void drawTank(){
        String[] colourList = this.colorsOfTank.split(",");
        int[] listOfColor = new int[colourList.length];
        for(int i = 0; i <colourList.length; i++){
            listOfColor[i] = Integer.parseInt(colourList[i]);
        }

        int lowerWidth = 24;
        float upperWidth = lowerWidth*0.6f;
        int lowerHeight = 12;
        float upperHeight = lowerHeight/2;
        this.sketch.fill(listOfColor[0], listOfColor[1], listOfColor[2]);
        this.sketch.rect(this.x - lowerWidth/2, this.y - lowerHeight/2, lowerWidth, lowerHeight, 40); //corner radius = 5
        this.sketch.rect(this.x-lowerWidth/2+(lowerWidth - upperWidth)/2, this.y-lowerHeight/2 - upperHeight, upperWidth, upperHeight, 40);

        //isolate the turret to only draw it
        this.sketch.pushMatrix();
        this.sketch.translate(this.x, this.y - lowerHeight - upperHeight/6);
        this.turretX = this.x;
        this.turretY = this.y - lowerHeight - upperHeight/6;
        //draw the turret
        this.sketch.rotate(this.angle);
        this.sketch.stroke(100, 100, 100);
        this.sketch.strokeWeight(3);
        this.sketch.line(0,0,0, -15);
        this.sketch.noStroke();
        this.sketch.popMatrix();
        drawExplosion();
        if(this.isThisPlayerTurn()){
            this.updateTurret(1.0f/this.sketch.frameRate);
            this.updateMovement(1.0f/this.sketch.frameRate);
            this.updatePower();
            this.renderPower();
            this.updateProjectile(1.0f/this.sketch.frameRate);
            this.fireProjectile();
            this.renderFuel();
            this.renderHealthBar();
            this.drawTurn();
            this.sketch.image(parachute, 175, 55);
            this.sketch.textSize(22);
            this.sketch.fill(0);
            this.sketch.text(String.valueOf(this.parachuteCount), 220, 90);
            this.sketch.noFill();
        }
    }
    public void drawTurn(){
        if(!isDead) {
            this.sketch.textSize(20);
            this.sketch.fill(0);
            this.sketch.text(this.currentState.generateReport(), 20, 32);
        }
    }

    public int getPoint(){
        return this.point;
    }
    public void setPoint(int point){
        if(!hasIncreasedPoint) {
            this.point += point;
            hasIncreasedPoint = true;
        }
    }
    public void increasePointByFallTerrain(int point){
        if(!hasIncreasedPointByFallTerrain){
            this.point += point;
            hasIncreasedPointByFallTerrain = true;
        }
    }
    public void drawExplosion(){
        Iterator<Explosion> it = this.tankExplosion.iterator();
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

    public boolean isThisPlayerTurn(){
        if(!isDead) {
            this.sketch.image(fuelImage, 180, 10);
            return this.playerNumber.equals(this.currentState.getCurrentPlayer());
        }
        return false;
    }
    public void renderFuel(){
        String text = String.valueOf(this.fuel);
        if(isThisPlayerTurn()){
            this.sketch.textSize(20);
            this.sketch.fill(0);
            this.sketch.text(text, 210, 32);
            this.sketch.noFill();
        }
    }
    public void renderHealthBar(){
        if(this.health <= 0){
            this.health = 0;
        }
        this.sketch.textSize(20);
        this.sketch.fill(0);
        this.sketch.text("Health: ", 375, 32);
        this.sketch.noFill();

        String[] colourList = this.colorsOfTank.split(",");
        int[] listOfColor = new int[colourList.length];
        for(int i = 0; i <colourList.length; i++){
            listOfColor[i] = Integer.parseInt(colourList[i]);
        }
        this.sketch.fill(listOfColor[0], listOfColor[1], listOfColor[2]);
        this.sketch.rect(450, 10, 180, 30);

        //The line
        this.sketch.fill(100, 100, 100);
        this.sketch.rect(450 + 180*this.health/100, 5, 5, 40);
        this.sketch.fill(0);
        this.sketch.textSize(24);
        this.sketch.text(String.valueOf(this.health), 650, 32);
        this.sketch.noFill();


    }
    public void renderPower(){
        this.sketch.fill(0);
        this.sketch.textSize(22);
        this.sketch.text("Power: ", 375, 70);
        String powerTank = String.valueOf((int) this.power);
        this.sketch.text(powerTank,450, 70);
        this.sketch.noFill();
    }
    public void updatePower(){
        float deltaTime = 1.0f/this.sketch.frameRate;
        if(this.power >= this.health){
            this.power = this.health;
        }
        else{
            if(this.sketch.keyPressed && this.sketch.key == 'w'){
                this.power += 36*deltaTime;
                this.sketch.key = 0;
            }
            if(this.sketch.keyPressed && this.sketch.key == 's'){
                this.power -= 36*deltaTime;
                this.sketch.key = 0;
            }
        }
    }
    public void fireProjectile() {

        if(isThisPlayerTurn() && this.sketch.keyPressed && this.sketch.key == ' ' && !hasFired) {
            Projectile newProjectile = new Projectile(this, this.setting, this.sketch, this.turretX, this.turretY, this.power*9/100, this.angle, this.averageHeight);
            this.projectile.add(newProjectile);
            this.sketch.key = 0;
        }
    }

    public void updateProjectile(float deltaTime){
        int windPower = this.currentState.getWind();
        Iterator<Projectile> it = this.projectile.iterator();
        if(this.projectile != null && !this.projectile.isEmpty()){
            boolean hasDone = true;
            while(it.hasNext()){
                Projectile p = it.next();
                if(!p.hasDoneFired()){
                    p.fire(1.0f/this.sketch.frameRate, windPower);
                    p.renderProjectile();
                    hasDone = false;
                }
                else{
                    it.remove();
                }
            }
            if(hasDone){
                hasFired = true;
            }
        }
    }
    public void endTurn(){
        //isHasDoneExplode();
        if(hasFired){
            if (!isDead) {
                this.currentState.nextTurn();
                hasFired = false;
                this.projectile.clear();
                hasBeenReduced = false;
                hasIncreasedPoint = false;
                hasBeenReducedByFall = false;
                hasIncreasedPointByFallTerrain = false;
            }


        }
    }
    public int getHealth(){
        return this.health;
    }


    //reduces by being hit
    public void setHealth(int damage){
        if(!hasBeenReduced) {
            this.health -= damage;
            hasBeenReduced = true;
        }
    }

    public void reduceHealthByFall(int damage){
        if(!hasBeenReducedByFall){
            this.health -= damage;
            hasBeenReducedByFall = true;
        }
    }
    public ArrayList<Projectile> getProjectile(){
        return this.projectile;
    }
    public int getParachuteCount(){
        return this.parachuteCount;
    }
    public void isHit(float newY){
        isHit = true;
    }
    public float getY(){
        return this.y;
    }

    public void reduceY(float deltaTime, float newY){
        if(this.y < newY && this.parachuteCount > 0) {
            this.renderParachute();
            if(!decreaseParachute) {
                this.parachuteCount -= 1;
                decreaseParachute = true;
            }
            this.y += deltaTime * 60.0f;
            if(this.y >= newY){
                //doneParachute = true;
                decreaseParachute = false;
            }
        }
        else if(this.parachuteCount == 0){
            this.y += deltaTime * 120.0f;
            if(this.y >= newY){
                //doneParachute = true;
                this.y = newY;
            }
        }
    }
    public void setY(float newY){
        this.y = newY;
    }
    public int getX(){
        return this.x;
    }
    public void setX(int newX){
        this.x = newX;
    }

    public void updateTurret(float deltaTime){
        float speed = 3.0f; //3 radians per second
        if(this.isThisPlayerTurn() && !endTurn) {
            if (this.sketch.keyPressed && this.sketch.keyCode == PConstants.UP) {
                if (this.angle > PConstants.HALF_PI) {
                    this.angle = PConstants.HALF_PI;
                } else if (this.angle < -PConstants.HALF_PI) {
                    this.angle = -PConstants.HALF_PI;
                }
                this.angle += speed * deltaTime;
            } else if (this.sketch.keyPressed && this.sketch.keyCode == PConstants.DOWN) {
                if (this.angle > PConstants.HALF_PI) {
                    this.angle = PConstants.HALF_PI;
                } else if (this.angle < -PConstants.HALF_PI) {
                    this.angle = -PConstants.HALF_PI;
                }
                this.angle -= speed * deltaTime;
            }
        }
    }
    public void updateMovement(float deltaTime){
        if(this.isThisPlayerTurn() && !endTurn) {
            if (this.sketch.keyCode == PConstants.RIGHT && this.sketch.keyPressed && this.fuel > 0) {
                this.goRight(deltaTime);
                this.fuel -= 1;
            } else if (this.sketch.keyCode == PConstants.LEFT && this.sketch.keyPressed && this.fuel > 0) {
                this.goLeft(deltaTime);
                this.fuel -= 1;
            }
        }
    }
    @Override
    public void goLeft(float deltaTime) {
        this.x -= 60*deltaTime;
        if(this.x < 0){
            this.x = 0;
        }
        else if(this.x > 864){
            this.x = 864;
        }
        this.y = this.averageHeight[this.x];
    }

    @Override
    public void goRight(float deltaTime) {
        this.x += 60*deltaTime;
        if(this.x < 0){
            this.x = 0;
        }
        else if(this.x > 864){
            this.x = 864;
        }
        this.y = this.averageHeight[this.x];
    }
    public String getCurrentPlayer(){
        return this.playerNumber;
    }
}
