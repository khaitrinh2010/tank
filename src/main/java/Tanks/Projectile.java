package Tanks;

import org.json.JSONObject;
import processing.core.PApplet;
import processing.core.PConstants;

import java.util.ArrayList;

public class Projectile extends Thing{
    //Initial fire position
    private float power;
    private ArrayList<Explosion> explosionList = new ArrayList<>();
    private float gravity = 3.6f; //gravity rate
    private float veloHori;
    private float veloVerti;
    private boolean hasFired = false;
    float[] terrainY;
    private Tank tank;
    private boolean ulti;
    /**
     * Constructs a Projectile object with specific parameters.
     *
     * @param tank The tank that fired the projectile.
     * @param x The initial x-coordinate of the projectile.
     * @param y The initial y-coordinate of the projectile.
     * @param power The power of the projectile which affects its velocity.
     * @param angle The firing angle of the projectile.
     * @param terrainHeight The height data of the terrain for collision detection.
     * @param fromUlti Indicates whether the projectile is fired as part of an ultimate ability.
     */
    public Projectile (Tank tank, float x, float y, float power, float angle, float[] terrainHeight, boolean fromUlti){
        super((int) x, y);
        this.tank = tank;
        this.power = power;
        angle -= PConstants.HALF_PI;
        this.veloHori =  this.power * (float) Math.cos(angle);
        this.veloVerti = this.power * (float) Math.sin(angle);
        terrainY = terrainHeight;
        ulti = fromUlti;
    }

    /**
     * Update the position of this projectile based on the power, velocity and wind power
     * @param windPower, an integer indicates the power of the wind affect the bullet
     */
    public void fire(int windPower){
        this.x += (int) (this.veloHori + windPower*0.03f);
        this.veloVerti += gravity /30;
        this.y += this.veloVerti;
    }

    /**
     * Draw the projectile
     * @param sketch, PApplet instance for drawing
     * @param projColour, an array with RGB values determine the colour of the projectile
     */
    public void renderProjectile(PApplet sketch, int[] projColour){
        if(!ulti) {
            sketch.fill(projColour[0], projColour[1], projColour[2]);
            sketch.ellipse(this.x, this.y, 12, 12);
        }
        else{
            sketch.fill(projColour[0], projColour[1], projColour[2]);
            sketch.ellipse(this.x, this.y, 18, 18);
        }
        sketch.fill(0);
        sketch.ellipse(this.x, this.y, 4, 4);
    }

    /**
     * Determines whether the projectile has hit a target, if yes, it will create an Explosion object
     * @param sketch, PApplet instance for drawing explosions.
     * @param terrainY, the heights array to check whether the bullet has hit the terrain
     * @return true if the projectile has hit the terrain, false otherwise.
     */
    public boolean hasDoneFired(PApplet sketch, float[] terrainY){
        if(this.y >= 640 || this.y <= 0 || this.x >= 864 || this.x <= 0){
            Explosion newExplosion = new Explosion(this.tank, sketch, this.x, this.y, 30, this);
            this.explosionList.add(newExplosion);
            return true;
        }
        for(int i = 0; i < terrainY.length; i++){
            int currentX = (int) (double) this.x;
            if(terrainY[currentX] <= this.y && !hasFired){
                Explosion newExplosion = null;
                if(!ulti) {
                    newExplosion = new Explosion(this.tank, sketch, this.x, this.y, 30, this);
                }
                else{
                    newExplosion = new Explosion(this.tank, sketch, this.x, this.y, 45, this);
                }
                this.tank.getTankExplosionList().add(newExplosion);
                return true;
            }
        }
        return false;

    }
}
