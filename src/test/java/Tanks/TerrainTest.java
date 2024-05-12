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
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class TerrainTest {
    App sketch;
    Level level1;
    Terrain terrain;
    Tank tank;
    Explosion explosion;
    HashSet<Explosion> testExplosions = new HashSet<>();
    float[] averageHeight;
    Tank opponent;

    @BeforeEach

    public void setup() {
        sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        level1 = sketch.allLevels.get(0);
        terrain = level1.initialiseEntities();
        tank = terrain.getTankList().get(0);
        opponent = level1.allLevels.get(0).getTanksList().get(1);
        averageHeight = terrain.getAverageHeight();
        sketch.delay(500);
        //Create a hole
        opponent.setX(390);
        opponent.setY(terrain.getAverageHeight()[390]);
        sketch.delay(1000);
    }
    @AfterEach
    public void clear(){
        sketch.noLoop();
        sketch.dispose();
    }

//    public void testHitADeadTank(){
//        level1.getState().setWind(0);
//        for(int i = 0; i < level1.getTanksList().size(); i++){
//            Tank tank = level1.getTanksList().get(i);
//            if(i == 1 || i == 3){
//                tank.setHealth(0);
//            }
//        }
//        sketch.delay(1000);
//
//        //Below I set up a scenario when the the current tank will hit the next tank like in the tank test casse
//        int initialPoint = tank.getPoint();
//        tank.setX(100);
//        tank.setY(terrain.getAverageHeight()[100]);
//        //Update the power
//        for(int i = 0; i < 30; i++){
//            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'w', 0));
//        }
//        for(int i = 0; i < 13; i++){
//            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'l', PConstants.UP));
//        }
//        sketch.delay(1000);
//        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 0));
//        //await for thing to be continued
//        sketch.delay(2000);
//        assertEquals(initialPoint, tank.getPoint(), "Tank's point should not increase when hit a dead tank");
//    }

    @Test
    public void testTankGoOffTerrainAndExplode(){
        for(int i = 100; i < 200; i++){
            terrain.setTerrain(i, 640);
        }
        for(int i = 0; i < level1.getTanksList().size(); i++){
            Tank tank = level1.getTanksList().get(i);
            if(i == 2 || i == 3){
                tank.setHealth(0);
            }
        }
        sketch.delay(1000);
        for(int i = 0; i < 40; i++){
            sketch.delay(10);
            sketch.keyPressed(new KeyEvent(null, 0,0,0,'y', PConstants.RIGHT));
        }
        sketch.delay(500);

        assertEquals(0, tank.getHealth(), "Tank should be explode when going off the terrain");
        sketch.noLoop();
        sketch.dispose();
    }

    @Test
    public void testSetHeight(){
        terrain.setTerrain(30, 80);
        assertEquals(terrain.getAverageHeight()[30], 80, "Fail to set the terrain height");
    }

}
