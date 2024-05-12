package Tanks;

import processing.core.PApplet;

public class Explosion extends Thing{
    private PApplet sketch;
    private float radius;
    private boolean hasDoneExplode = false;
    private int time = 200; //which  is 0.2 seconds
    private float startTime;
    private Tank tank;
    public Explosion (Tank tank, PApplet sketch, float x, float y, float radius, Projectile projectile){
        super((int) x, y);
        this.tank = tank;
        this.sketch = sketch;
        this.radius = radius;
        this.startTime = this.sketch.millis();
    }

    /**
     * Retrieves the tank that cause the explosion
     * @return The tank that caused this explosion
     */
    public Tank getTank(){
        return this.tank;
    }
    /**
     * Draw the explosion, which expands over time until it reaches its limit
     */
    public void drawExplosion(){
        if(!hasDoneExplode){
            float currentTime = this.sketch.millis();
            if(currentTime >= (this.startTime + time)){
                hasDoneExplode = true;
            }
            else {
                this.sketch.fill(255,0, 0);
                this.sketch.ellipse(this.x, this.y, this.radius*2*(currentTime - this.startTime)/200, this.radius*2*(currentTime-startTime)/200);

                this.sketch.fill(255, 150, 0);
                this.sketch.ellipse(this.x, this.y, this.radius*(currentTime - this.startTime)/200, this.radius*(currentTime-startTime)/200);

                this.sketch.fill(255, 255, 0);
                this.sketch.ellipse(this.x, this.y, this.radius*0.4f*(currentTime-this.startTime)/200, this.radius*0.4f*(currentTime-startTime)/200);
            }
        }
    }

    /**
     * Return the radius of this explosion
     * @return a float represents the radius of this explosion
     */
    public float getRadius(){
        return this.radius;
    }
    /**
     * Check if this explosion has completed the animation
     * @return true if it has completed, false otherwise
     */
    public boolean isHasDoneExplode(){
        return hasDoneExplode;
    }
}
