package Tanks;

import org.json.JSONObject;
import processing.core.PApplet;

public class Thing {
    private JSONObject parseJSON;
    private PApplet sketch;
    public Thing(JSONObject parseJSON, PApplet sketch){
        this.parseJSON  = parseJSON;
        this.sketch = sketch;
    }
}
