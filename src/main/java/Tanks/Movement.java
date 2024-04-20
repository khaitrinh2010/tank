package Tanks;

import processing.core.PConstants;

//input handler class
public interface Movement {
    void goLeft(float deltaTime);

    void goRight(float deltaTime);
}
