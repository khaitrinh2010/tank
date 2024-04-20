package Tanks;

import org.json.JSONObject;
import processing.core.PApplet;
import processing.core.PConstants;

import java.util.ArrayList;
import java.util.Arrays;

public class Projectile extends Thing{
    //Initial fire position
    private float x;
    private float y;
    private float initialHeight;
    private float power;
    private ArrayList<Explosion> explosionList = new ArrayList<>();
    private float gravity = 3.6f; //gravity rate
    private PApplet sketch;
    JSONObject parseJson;
    private float veloHori;
    private float veloVerti;
    private boolean hasFired = false;
    private boolean hasDone;
    float[] terrainY;
    private Tank tank;
    //rsin how quick it moves related to the y axis, rcos how quick it moves related to the x axis
    public Projectile (Tank tank, JSONObject parseJson, PApplet sketch, float x, float y, float power, float angle, float[] terrainHeight){
        super(parseJson, sketch);
        this.tank = tank;
        this.sketch = sketch;
        this.x = x;
        this.y = y;
        initialHeight = this.y;
        this.power = power;
        angle -= PConstants.HALF_PI;
        this.veloHori =  this.power * (float) Math.cos(angle);
        this.veloVerti = this.power * (float) Math.sin(angle);
        terrainY = terrainHeight;
        this.hasDone = true;
    }
    public void handleError(){
        if(this.x > 864){
            this.x = 864;
        }
        if(this.x < 0){
            this.x = 0;
        }
        if(this.y > 640){
            this.y = 640;
        }
        if((this.y) < 0){
            this.y = 0;
        }
    }
    public void fire(float deltaTime, int windPower){
        this.x += (float) (this.veloHori + windPower*0.03f);
        this.veloVerti += gravity*deltaTime;
        this.y += this.veloVerti;
        //handleError();
    }
    public void renderProjectile(){
        //handleError();
        this.sketch.fill(100, 100, 100);
        this.sketch.ellipse(this.x, this.y, 12, 12); //draw a bullet of 12,12
    }
    public float getX(){
        return this.x;
    }
    public float getY(){
        return this.y;
    }
    public boolean hasDoneFired(){
        handleError();
        if(this.y >= 640 || this.y <= 0 || this.x >= 864 || this.x <= 0){
            Explosion newExplosion = new Explosion(this.tank, this.sketch, this.x, this.y, 30);
            this.explosionList.add(newExplosion);
            return true;


        }
        for(int i = 0; i < terrainY.length; i++){
            int currentX = (int) Math.floor(this.x);
            if(terrainY[currentX] <= this.y && !hasFired){
                Explosion newExplosion = new Explosion(this.tank, this.sketch, this.x, this.y, 30);
                this.tank.getTankExpllosionList().add(newExplosion);
                return true;

            }
        }
        return false;

    }

    public ArrayList<Explosion>  getExplosionList(){
        return this.explosionList;
    }

}
