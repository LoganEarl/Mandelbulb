package scenes;

import processing.core.PApplet;

import java.util.Locale;

public abstract class Scene extends PApplet {
    private int tempWidth, tempHeight;

    public Scene(int width, int height){
        this.tempWidth = width;
        this.tempHeight = height;
    }

    @Override
    public void settings() {
        size(tempWidth, tempHeight);
    }

    public String[] getArgs() {
        return new String[]{this.getClass().getName()};
    }

    public void saveFrame(int timeIndex){
        super.saveFrame(String.format(Locale.US, "frames/%s-%06d.png", this.getClass().getSimpleName(), timeIndex));
    }
}
