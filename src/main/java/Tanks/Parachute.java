package Tanks;

import processing.core.PApplet;
import processing.core.PImage;

public class Parachute {
    private float x; private float y; private PApplet sketch; private PImage image; private Tank tank;
    public Parachute(Tank tank, PApplet sketch, float x, float y, String imagePath){
        this.x = x;
        this.y = y;
        this.sketch = sketch;
        this.image = this.sketch.loadImage(imagePath);
        this.image.resize(30,30);
        this.tank = tank;
    }
    public void renderParachute(){
        this.sketch.image(this.image, this.x, this.y);
    }
}
