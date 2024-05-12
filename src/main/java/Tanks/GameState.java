package Tanks;
import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.*;

import static java.lang.Math.abs;

public class GameState {
    private int totalPlayers;
    private String currentWorkingDirec = System.getProperty("user.dir");
    String windImagePath = currentWorkingDirec + "/src/main/resources/Tanks/wind.png";
    String windImagePath2 = currentWorkingDirec + "/src/main/resources/Tanks/wind-1.png";
    private int index = 0;
    private PApplet sketch;
    public ArrayList<Tank> tankArrayList;
    private PImage windImage; private PImage windImage2;
    Random random = new Random();
    private int[] colors;
    private Level level;
    private int transistion = 1000;
    private int start = 0;
    private int transitionTime = 0;
    private int endTime = 700;
    private int idx = 0;
    private ArrayList<PImage> assets = new ArrayList<>();
    private int initialWind = random.nextInt(71) - 35;

    /**
     * Represents the current state of the game, keep track of tanks, environmental effects and things that shared between tanks
     * Each level has its own state
     */
    public GameState(Level level, PApplet sketch){
        this.sketch = sketch;
        this.level = level;
    }

    /**
     * Set the tank list of the game participating in this current level and sort them
     * @param tankslist an ArrayList of Tank object includes tanks that are managed
     */
    public void setTankList(ArrayList<Tank> tankslist){
        this.tankArrayList = tankslist;
        Collections.sort(this.tankArrayList);
        totalPlayers = this.tankArrayList.size();
    }

    /**
     * Set the assets used for shared visual elements in the game such as wind image, fuel image, parachute image
     * @param images the list of images that being shared across all tanks in the level
     */
    public void setAssets(ArrayList<PImage> images){
        assets = images;
    }
    /**
     * Draw shared visual elements of the game
     */
    public void drawSharedThings(){
        PImage fuel = assets.get(0);
        PImage parachute = assets.get(1);
        fuel.resize(32, 32);
        parachute.resize(42, 42);
        this.sketch.image(parachute, 175, 55);
        this.sketch.image(fuel, 180, 10);
    }
    /**
     * Draw the scoreboard, both when the game is over orr in the game
     */
    public void drawScoreBoard() {
        if(this.level.isGameOver()){
            float x = 864*0.35f;
            float y = 640*0.25f;
            float recHeight = 640*0.35f;
            float z = y + 60;
            this.sketch.stroke(0);
            this.sketch.fill(0);
            this.sketch.text("Final Scores", x, y + 50);
            this.sketch.line(x, z, x + 300, z);
            this.sketch.noFill();
            int[] colors;
            ArrayList<Tank> tankArray = this.tankArrayList;
            tankArray.sort((tank1, tank2) -> tank2.getPoint() - tank1.getPoint());
            Tank winner = this.tankArrayList.get(0);
            colors = winner.getColourList();
            this.sketch.fill(colors[0], colors[1], colors[2]);
            this.sketch.text("Player " + winner.getPlayerNumber() + " wins!", x, y);
            this.sketch.noFill();
            this.sketch.fill(colors[0] ,  colors[1],  colors[2], 50);
            this.sketch.stroke(0);
            this.sketch.strokeWeight(4);

            this.sketch.rect(x, y + 30, 300, recHeight);
            transitionTime += 700.0f / this.sketch.frameRate;

            if (transitionTime >= endTime) {
                transitionTime = 0;
                idx++;
                if(idx >= tankArray.size()){
                    idx = tankArray.size() - 1;
                }
            }
            for (int i = 0; i <= idx; i++) {
                Tank tank = tankArray.get(i);
                int[] tanksColour = tank.getColourList();
                int r = tanksColour[0]; int g = tanksColour[1]; int b = tanksColour[2];
                this.sketch.fill(r,g,b);
                String text = "Player" + tank.getPlayerNumber();
                this.sketch.text(text, x + 25, y + 95 + i*45);
                this.sketch.text(String.valueOf(tank.getPoint()), x + 240, y + 95 + i*45);
                this.sketch.noFill();
            }
            this.sketch.noFill();
            this.sketch.noStroke();
            //drawSharedThings();
            return;
        }
        windImage = this.sketch.loadImage(windImagePath);
        windImage.resize(40,40);
        windImage2 = this.sketch.loadImage(windImagePath2);
        windImage2.resize(40,40);
        this.drawWind(windImage, windImage2);
        this.sketch.textSize(24);
        this.sketch.fill(0);
        this.sketch.text(String.valueOf(abs(initialWind)), 800, 40);
        this.sketch.noFill();
        this.sketch.stroke(5);
        this.sketch.textSize(20);
        this.sketch.strokeWeight(4);
        this.sketch.rect(650, 50, 200, 140);
        this.sketch.text("Scores", 670, 75);
        this.sketch.line(650, 80, 850, 78);
        drawDetail();
        drawSharedThings();
    }
    /**
     * draws the normal scoreboard when the game is not over, gets called by the previous function
     */
    public void drawDetail(){
        float y = 100;
        for(int i = 0; i < this.tankArrayList.size(); i ++){
            //if(this.tankArrayList.get(i).getHealth() > 0) {
            colors = this.tankArrayList.get(i).getColourList();
            String text = "Player " + this.tankArrayList.get(i).getPlayerNumber();
            this.sketch.fill(colors[0], colors[1], colors[2]);
            this.sketch.text(text, 670, y);
            this.sketch.text(String.valueOf(this.tankArrayList.get(i).getPoint()), 800, y);
            this.sketch.noFill();
            y += 25;
            if (y == totalPlayers - 1) {
                y = 40;
            }
            //}
        }
        this.sketch.noStroke();
    }
    /**
     * Retrieves which turn it is
     * @return a String that displayed on the top left of the screen
     */
    public String generateReport(){
        return "Player " + this.getCurrentPlayer() + "'s " + "turn";
    }
    /**
     * return the current player in turn, compare with the isThisPlayerTurn() in the tank class to decide actions of the tank
     * @return a String that represents the current player number
     */
    public String getCurrentPlayer(){
        Collections.sort(this.tankArrayList);
        if(this.tankArrayList.isEmpty()){
            return "No Player Found";
        }
        return this.tankArrayList.get(index).getPlayerNumber();
    }
    /**
     * Check if the game is over based on the health and user's input
     * @return true if the current level is over, false otherwise
     */
    public boolean isGameOver(){
        int count = 0;
        if(this.tankArrayList != null) {
            for (Tank tank : this.tankArrayList) {
                if (tank.getHealth() > 0) {
                    count++;
                }
            }
            if (count == 1 || count == 0) {
                if(this.sketch.keyPressed && this.sketch.key == ' '){
                    return true;
                }
                if(start > transistion) {
                    index = 0;
                    return true;
                }
                else{
                    start += (int) (1000.0f/this.sketch.frameRate);
                }
            }
        }

        return false; //not yet end the game
    }
    /**
     * Decide which player's turn is next, update the active player
     */
    public void nextTurn(){
        Collections.sort(this.tankArrayList);
        int count = 0;
        index = (index+1)  % (this.tankArrayList.size());
        //find the next player that is alive
        for (Tank tank : this.tankArrayList) {
            if (tank.getHealth() > 0) {
                count++;
            }
        }
        if(count >= 1){
        while(true) {
            if (this.tankArrayList.get(index).getHealth() > 0) {
                break;
            }
            index = (index + 1) % (this.tankArrayList.size());
        }
        }
        initialWind += random.nextInt(11) - 5;
    }
    /**
     * Draw the wind image
     * @param windImage an image used the wind is blowing right
     * @param windImage2 an image used the wind is blowing left
     */
    public void drawWind(PImage windImage, PImage windImage2){
        if(this.getWind() >= 0){
            this.sketch.image(windImage, 750, 10);
        }
        if(this.getWind() < 0){
            this.sketch.image(windImage2, 750, 10);
        }
    }

    /**
     * return the current wind of this turn
     * @return an integer represents the power of the wind in this turn
     */
    public int getWind() {
        return initialWind; //no fluctuation in wind power
    }
    /**
     * set the wind of this turn to a new number
     * @param newWind aan integer represents the new wind power
     */
    public void setWind(int newWind){
        initialWind = newWind;
    }


}
