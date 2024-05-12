package Tanks;


import org.checkerframework.checker.units.qual.K;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class LevelTest {

    App sketch;
    Level level1;
    Level level2;
    Level level3;
    Terrain terrain;
    ArrayList<Level> levels;
    @BeforeEach
    public void setup(){
        sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        level1 = sketch.allLevels.get(0);
        level2 = sketch.allLevels.get(1);
        level3 = sketch.allLevels.get(2);
        terrain = sketch.allTerrains.get(0);


    }
    @AfterEach
    public void clear(){
        sketch.noLoop();
        sketch.dispose();
    }

    @Test
    public void testParseJSONFile () {
        JSONObject result = level1.parseJSONFile();
        JSONObject expected = new JSONObject();
        expected.put("layout", "level1.txt");
        expected.put("background", "snow.png");
        expected.put("foreground-colour", "255,255,255");
        expected.put("trees", "tree2.png");
        assertEquals(result.toString(), expected.toString(), "There is an error");
    }
    @Test
    public void testUpdateTankList(){
        /*The updateTankList will called when moving to the next level, so initially, the tank array size of level 2 is 0
        When level 1 is done, it updated, now I will test if level 1 done then the tankArrayList size in level 2 is the same
        **/
        for(Tank tank: level1.getTanksList()){
            tank.setHealth(0); // So the level will end
        }
        sketch.delay(2000);
        assertEquals(level2.getTanksList().size(), level1.getTanksList().size());

    }
    @Test
    public void testComputeAverage(){
        float[] heights = new float[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};
        float result = level1.computeAverage(0, heights);
        assertEquals(result, 16.5, "Compute average incorrectly");
    }
    @Test
    public void testAssetsLoading(){
        ArrayList<PImage> testedAssets = level1.getAssets();
        assertFalse(testedAssets.isEmpty());
    }
    @Test
    public void testInitialiseTerrain(){
        Terrain level1Terrain = sketch.allTerrains.get(0);
        float[] result = level1.getAverageHeight();
        float[] expected = level1Terrain.getAverageHeight();
        assertEquals(Arrays.toString(result), Arrays.toString(expected));
    }

    @Test
    public void testTransitionLevel(){
        level1.getState().setWind(0);
        sketch.delay(100);
        int oldLLevelNumber = sketch.getCurrentLevel().getLevelNumber();
        for(int i = 0; i < level1.getTanksList().size(); i++){
            Tank tank = level1.getTanksList().get(i);
            if(i == 0){
                tank.setX(100);
                tank.setY(terrain.getAverageHeight()[100]);
            }
            else if(i == 1){
                tank.setHealth(5);
                tank.setX(390);
                tank.setY(terrain.getAverageHeight()[390]);
            }
            else {
                tank.setHealth(0);
            }
        }
        //Set up the scenario whhen only 2 tanks remain
        for(int i = 0; i < 30; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'w', 0));
        }
        for(int i = 0; i < 13; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'l', PConstants.UP));
        }
        sketch.delay(1000);
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 0));

        //await for thing to be continued
        sketch.delay(5000);
        //Moving to the next level
        assertTrue(level1.isDoneLevel(), "Level should end after only 1 tank is alive");
    }
    @Test
    public void testGameOver(){
        //Set the 2 previous levels to be false
        level1.setDoneLevel(true);
        level2.setDoneLevel(true);
        level3.getState().setWind(0);
        sketch.delay(100);
        for(int i = 0; i < level3.getTanksList().size(); i++){
            Tank tank = level3.getTanksList().get(i);
            if(i != 2 && i != 1){
                tank.setHealth(0);
            }
            else if(i == 1){
                tank.setParachuteCount(0);
                tank.setHealth(10);
            }
        }
        for(int i = 0; i < 30; i++){
            sketch.keyPressed(new KeyEvent(null, 0,0,0,'s',0));
        }
        sketch.delay(2000);
        //Kill the tank itself
        sketch.keyPressed(new KeyEvent(null, 0,0,0,' ',0));
        sketch.delay(2000);
        assertTrue(level3.isGameOver(), "The game should be over after the last level ends");

        sketch.delay(2000);
    }

    @Test
    public void testResetGame(){
        level1.setDoneLevel(true);
        level2.setDoneLevel(true);
        level3.setDoneLevel(true);
        //give time to draw the final scoreboard
        sketch.delay(3300);
        sketch.keyPressed(new KeyEvent(null, 0,0,0,'r',0));
        sketch.delay(2000);
        assertEquals(level1.getLevelNumber(), sketch.allLevels.get(0).getLevelNumber(), "The game should be reset after press r");
    }
}
