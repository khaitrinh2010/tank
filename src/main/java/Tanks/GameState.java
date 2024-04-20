package Tanks;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.*;
public class GameState {
    private int totalPlayers;
    private String currentWorkingDirec = System.getProperty("user.dir");
    String windImagePath = currentWorkingDirec + "/src/main/resources/Tanks/wind.png";
    String windImagePath2 = currentWorkingDirec + "/src/main/resources/Tanks/wind-1.png";
    private int index = 0;
    private ArrayList<String> playerList;
    private PApplet sketch;
    private int wind;
    private ArrayList<Tank> tankArrayList;
    public HashSet<Tank> tankList;
    private int removed = -1;
    private PImage windImage; private PImage windImage2;
    Random random = new Random();
    private int initialWind = random.nextInt(71) - 35;
    private boolean isEndOfTurn = false; // denotes when to change the wind power
    public GameState(PApplet sketch, ArrayList<String> playerNumbers){
        this.sketch = sketch;
        this.totalPlayers = playerNumbers.size();
        this.playerList = playerNumbers;
        this.tankList = new HashSet<>();
        this.windImage = this.sketch.loadImage(windImagePath);
        this.windImage.resize(40,40);
        this.windImage2 = this.sketch.loadImage(windImagePath2);
        this.windImage2.resize(40,40);
    }

    public void drawScoreBoard(){
        this.drawWind(this.windImage, this.windImage2);
        this.sketch.textSize(24);
        this.sketch.fill(0);
        this.sketch.text(String.valueOf(initialWind), 800, 40);
        this.sketch.noFill();
        tankArrayList = new ArrayList<>(this.tankList);
        Collections.sort(tankArrayList);
        this.sketch.stroke(5);
        this.sketch.textSize(20);
        this.sketch.text("Scores", 700, 80);
        float y = 100;
        for(int i = 0; i < this.tankList.size(); i ++){
            String text = "Player " + tankArrayList.get(i).getCurrentPlayer();
            this.sketch.text(text, 700, y);
            this.sketch.text(String.valueOf(tankArrayList.get(i).getPoint()), 800, y);
            y += 25;
            if(y == this.totalPlayers - 1){
                y = 40;
            }
        }
        this.sketch.noStroke();
    }
    public void deleteTank(Tank tank){
        Iterator<Tank> it = this.tankList.iterator();
        while(it.hasNext()){
            Tank newTank = it.next();
            if(newTank.getCurrentPlayer().equals(tank.getCurrentPlayer())){
                removed = tankArrayList.indexOf(newTank);
                it.remove();
            }
        }
    }

    public String generateReport(){
        String text = "Player " + this.getCurrentPlayer() + "'s " + "turn";
        return text;
    }
    public String getCurrentPlayer(){
        Collections.sort(tankArrayList);
        if(removed != -1){ //There's an tank has been removed
            if(removed <= index && index > 0){
                index -= 1;
            }
        }
        if(index == tankArrayList.size() - 1){
            isEndOfTurn =  true;
        }
        removed = -1;
        return tankArrayList.get(index).getCurrentPlayer();
    }
    public boolean isGameOver(){
        if(tankArrayList != null && tankArrayList.size() == 1){
            return true;
        }
        return false; //not yet end the game
    }
    public void nextTurn(){
        Collections.sort(tankArrayList);
        if(removed != -1){ //There's an tank has been removed
            if(removed <= index & index > 0){
                index -= 1;
            }
        }
        if(index == tankArrayList.size() - 1){
            isEndOfTurn =  true;
        }
        removed = -1;
        index = (index+1)% (tankArrayList.size());
        initialWind += random.nextInt(11) - 5;
    }
    public void drawWind(PImage windImage, PImage windImage2){
        if(this.getWind() >= 0){
            this.sketch.image(windImage, 750, 10);
        }
        if(this.getWind() < 0){
            this.sketch.image(windImage2, 750, 10);
        }
    }
    public int getWind() {
        return initialWind; //no fluctuation in wind power
    }

}
