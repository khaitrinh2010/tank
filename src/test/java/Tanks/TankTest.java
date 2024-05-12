package Tanks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;
import static processing.core.PApplet.*;

import java.util.ArrayList;
import java.util.Arrays;

public class TankTest {

    @Test
    public void testUltimate(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        tank.setUltilevel(3);
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 0));
        sketch.delay(100);
        assertEquals(tank.getProjectile().size(), 5, "When a tank hits the ultimate, it should fire 5 bullets at the same time");
        sketch.delay(2000);
    }

    @Test
    public void testUpdateTurret(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        float initialAngle = tank.getAngle();
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.UP));
        //1 second is 3 radian so 1 frame is 3/30;
        float expected = (float) (initialAngle + 3.0/30);
        assertEquals(expected, tank.getAngle(), "Tank's turret rotate wrongly");

        float currentAngle = tank.getAngle();
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.DOWN));
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.DOWN));

        //2 clicks so 6/30 radian;
        assertEquals(currentAngle - 6.0/30, tank.getAngle(), 0.01, "Tank's turret rotate wrongly");
        sketch.noLoop();
    }

    @Test
    public void testPowerUp(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        tank.setPoint(200);
        tank.setHealth(50);
        tank.setFuel(0);
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'r', 0));
        assertEquals(70, tank.getHealth(), "Health should be increased");
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'f', 0));
        assertEquals(200, tank.getFuel(), "Fuel should be increased");
        assertEquals(170, tank.getPoint(), "Point should be decreased to buy");
        sketch.noLoop();
        sketch.dispose();
    }
    @Test
    public void testUpdateMovementRight(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        int initialX = tank.getX();
        for(int i = 0; i < 5; i++) {
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.RIGHT));
        }
        assertEquals(initialX + 60/30*5, tank.getX(), "After moving right, tank's location is wrong");
        assertEquals(terrain.getAverageHeight()[initialX + 60/30*5], tank.getY(), 1.0, "After moving right, tank's location is wrong");
        sketch.noLoop();
        sketch.dispose();
    }
    @Test
    public void testUpdateMovementLeft() {
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        tank.setFuel(200);
        int currentX = tank.getX();
        for (int i = 0; i < 5; i++) {
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.LEFT));
        }
        assertEquals(currentX - 60 / 30 * 5, tank.getX(), "After moving left, tank's location is wrong");
        assertEquals(terrain.getAverageHeight()[currentX - 2 * 5], tank.getY(), "After moving left, tank's location is wrong");
        sketch.noLoop();
        sketch.dispose();
    }
    @Test
    public void testMovementToBoundary(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        tank.setX(10);
        tank.setY(terrain.getAverageHeight()[10]);
        for(int i = 0; i < 7; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.LEFT));
        }
        assertEquals(0, tank.getX(), "Tank should not move below 0 coordinate");
        tank.setX(858);
        tank.setY(terrain.getAverageHeight()[858]);
        for(int i = 0; i < 7; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.RIGHT));
        }
        assertEquals(864, tank.getX(), "Tank should not move out of boundary");
        sketch.noLoop();
        sketch.dispose();
    }
    @Test
    public void testUpdateTurretToBoundary(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        tank.setAngle(1.5F);
        //Click 20 times;
        for(int i = 0; i < 20; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.UP));
        }
        float difference = abs(PConstants.HALF_PI - tank.getAngle());
        assertTrue(difference <= 0.15, "Turret should only rotate 180 degrees");
        tank.setAngle(-1.5F);
        for(int i = 0; i < 20; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.DOWN));
        }
        float diff = abs(-PConstants.HALF_PI - tank.getAngle());
        assertTrue(diff <= 0.15, "Turret should only rotate 180 degrees");
        sketch.noLoop();
        sketch.dispose();
    }
    @Test
    public void testHitOpponentWithParachute(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        Tank opponent = level1.allLevels.get(0).getTanksList().get(1);
        opponent.setX(390);
        opponent.setY(terrain.getAverageHeight()[390]);
        int initialHealth = opponent.getHealth();
        int initialPoint = tank.getPoint();
        tank.setX(100);
        tank.setY(terrain.getAverageHeight()[100]);
        level1.getState().setWind(0);
        //Update the power
        for(int i = 0; i < 30; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'w', 0));
        }
        for(int i = 0; i < 13; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'l', PConstants.UP));
        }
        sketch.delay(1000);
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 0));
        //await for thing to be continued
        sketch.delay(2000);
        assertEquals(tank.getPoint() - initialPoint, initialHealth - opponent.getHealth(), "Tank's point or health update incorrectly");

    }
    @Test
    public void testHitOpponentWithoutParachute(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        Tank opponent = level1.allLevels.get(0).getTanksList().get(1);
        opponent.setX(390);
        opponent.setY(terrain.getAverageHeight()[390]);
        //Here I setup a scenario where similar to the test before, except that
        level1.getState().setWind(0);
        opponent.setParachuteCount(0);
        tank.setX(100);
        tank.setY(terrain.getAverageHeight()[100]);
        int initialHealth = opponent.getHealth();
        int initialPoint = tank.getPoint();
        float opponentInitialY = opponent.getY();
        for(int i = 0; i < 30; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'w', 0));
        }
        //I setup the turret like this so it wont shoot to the tank directly, instead it hit the terrain at the bottom of
        //the tank so the opponent will lose health by fall.
        for(int i = 0; i < 14; i++){
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'l', PConstants.UP));
        }
        sketch.delay(1000);
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', 0));
        sketch.delay(3000);
        float afterY = opponent.getY();
        float damageByFall = afterY - opponentInitialY;
        assertEquals(initialHealth - opponent.getHealth(), (int) damageByFall, 3, "Tank's health is not corrected when fall with out parachute");
        sketch.noLoop();
        sketch.dispose();
    }
    @Test
    public void testPowerUpExceedInValid(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        tank.setPoint(21);
        tank.setHealth(85);
        tank.setFuel(0);
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'r', 0));
        assertEquals(100, tank.getHealth(), "Tank's health should not exist 100");
        assertEquals(1, tank.getPoint(), "Point is decreased incorrectly");
        int initialHealth = tank.getHealth();
        int initialFuel = tank.getFuel();
        tank.setPoint(9);
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'r', 0));
        assertEquals(initialHealth, tank.getHealth());
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'f', 0));
        assertEquals(initialFuel, tank.getFuel());
        assertEquals(9, tank.getPoint());
        sketch.noLoop();
        sketch.dispose();
    }
    @Test
    public void testWithIrrelevantKey(){
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        int initialHealth = tank.getHealth();
        int initialFuel =  tank.getFuel();
        float initialAngle = tank.getAngle();
        int initialX = tank.getX();
        float initialY = tank.getY();
        sketch.keyPressed(new KeyEvent(null, 0, 0, 0, 'p', 0));
        assertEquals(initialAngle, tank.getAngle());
        assertEquals(initialFuel, tank.getFuel());
        assertEquals(initialY, tank.getY());
        assertEquals(initialX, tank.getX());
        assertEquals(initialHealth, tank.getHealth());
        sketch.noLoop();
        sketch.dispose();
    }
    @Test
    public void testMovementWithNoFuel()
    {
        App sketch = new App();
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        tank.setFuel(0);
        int initialX = tank.getX();
        for(int i = 0; i < 5; i++) {
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.LEFT));
        }
        assertEquals(initialX, tank.getX(), "No movement is allowed with less than or equal to 0 fuel");
        for(int i = 0; i < 5; i++) {
            sketch.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', PConstants.RIGHT));
        }
        assertEquals(initialX, tank.getX(), "No movement is allowed with less than or equal to 0 fuel");
        sketch.noLoop();
        sketch.dispose();
    }

    @Test
    public void testRandomColor(){
        App sketch = new App();
        sketch.setConfigPath("configTest.json");
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        sketch.noLoop();
        sketch.dispose();
    }

    @Test
    public void testUltimateImage(){
        App sketch = new App();
        sketch.setConfigPath("configTest.json");
        sketch.loop();
        PApplet.runSketch(new String[]{"App"}, sketch);
        sketch.delay(1000);
        Level level1 = sketch.allLevels.get(0);
        Terrain terrain = level1.initialiseEntities();
        Tank tank = terrain.getTankList().get(0);
        tank.setUltilevel(3);
        sketch.delay(2000);
        sketch.noLoop();
        sketch.dispose();sketch.noLoop();
        sketch.dispose();
    }

}
