package com.pantherman594;

import java.util.HashMap;
import java.util.Map;

class FunctionBox {
    private final Grapher grapher = Grapher.getInstance();

    private String PREFIX = "f(x, y) = ";
    private int GLOBAL_PADDING_TOP = 80;
    private int WIDTH = 200;
    private int HEIGHT = 20;
    private int PADDING_LEFT = 10;
    private int PADDING_BOTTOM = 5;
    private int TEXT_PADDING = 5;
    private int ERROR_DIAMETER = 10;

    private boolean isSetting = false;

    private int index;
    private String text = "";
    private String error = "";
    private Map<String, Float> points = new HashMap<>();
    private String genKey = "";

    FunctionBox(int index) {
        this.index = index;
    }

    FunctionBox(int index, boolean isSetting, String prefix) {
        this.index = index;
        this.isSetting = isSetting;
        PREFIX = prefix + ": ";
        WIDTH = 100;
    }

    // Draw the box and its text in the correct position, based on index
    void draw(boolean isFocused) {
        int x = PADDING_LEFT;
        int y = GLOBAL_PADDING_TOP + index * (HEIGHT + PADDING_BOTTOM);

        grapher.fill(200, 200, 200);
        if (isFocused) grapher.fill(250, 250, 250);
        grapher.rect(x, y, WIDTH, HEIGHT);

        x += TEXT_PADDING;
        y += HEIGHT - TEXT_PADDING;
        grapher.fill(0);
        grapher.text(PREFIX + text, x, y);

        x += WIDTH;
        if (error.length() > 0) { // If an error message exists,
            grapher.fill(255, 0, 0); // show a little red icon
            grapher.ellipse(x + ERROR_DIAMETER / 2, y - ERROR_DIAMETER / 2, ERROR_DIAMETER, ERROR_DIAMETER);

            // If the user's mouse hovers over that icon, show the error message
            if (grapher.mouseX > x && grapher.mouseX < x + ERROR_DIAMETER &&
                    grapher.mouseY > y - HEIGHT && grapher.mouseY < y) {
                grapher.text("ERROR: " + error, x + ERROR_DIAMETER * 1.5f, y);
            }

            grapher.fill(255);
            grapher.text("!", x + ERROR_DIAMETER / 2 - 1, y);
        }
    }

    // Check if the box was clicked
    boolean wasClicked() {
        int x = PADDING_LEFT;
        int y = GLOBAL_PADDING_TOP + index * (HEIGHT + PADDING_BOTTOM);

        return grapher.mouseX > x && grapher.mouseX < x + WIDTH && grapher.mouseY > y && grapher.mouseY < y + HEIGHT;
    }

    void error(String message) {
        error = message;
    }

    String getExpression() {
        return text;
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    boolean isSetting() {
        return isSetting;
    }

    String getSettingName() {
        return PREFIX.substring(0, PREFIX.length() - 2);
    }

    Map<String, Float> getPoints() {
        return points;
    }

    void setPoints(Map<String, Float> points) {
        this.points = points;
    }

    String getGenKey() {
        return genKey;
    }

    void setGenKey(String genKey) {
        this.genKey = genKey;
    }
}
