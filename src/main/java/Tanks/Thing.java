package Tanks;

import org.json.JSONObject;
import processing.core.PApplet;

public class Thing {
    protected int x; protected float y;
    public Thing(int x, float y){
        this.x = x;
        this.y  = y;
    }
    public void setX(int x){
        this.x = x;
    }
    public void setY(float y){
        this.y = y;
    }
    public float getY(){
        return this.y;
    }
    public int getX(){
        return this.x;
    }
}
