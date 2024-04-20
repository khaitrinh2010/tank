package Tanks;

import processing.core.PApplet;

public class Explosion {
    private PApplet sketch;
    private float x;
    private float y;
    private float radius;
    private boolean hasDoneExplode = false;
    private int time = 200; //which  is 0.2 seconds
    private float startTime;
    private int count = 0;
    private boolean active = true;
    private boolean hasBeenProcessed = false;
    private Tank tank;
    public Explosion(Tank tank, PApplet sketch, float x, float y, float radius){
        this.tank = tank;
        this.sketch = sketch;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.startTime = this.sketch.millis();
    }
    public Tank getTank(){
        return this.tank;
    }
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
    public float getX(){
        return this.x;
    }
    public float getY(){
        return this.y;
    }
    public float getRadius(){
        return this.radius;
    }
    public boolean isHasDoneExplode(){
        return hasDoneExplode;
    }
}
