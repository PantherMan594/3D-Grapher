// 3D Grapher, created by David Shen using Java and Processing

package com.pantherman594;

import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.pantherman594.Settings.*;

public class Grapher extends PApplet {

    private float transX, transY, transZ;
    private float rotateY, rotateZ;

    private int prevMouseX = 0;
    private int prevMouseY = 0;

    private ArrayList<FunctionBox> functionBoxes = new ArrayList<>();
    private int focusedBoxIndex = -1;

    private boolean rotate = false;
    private boolean draw = true;
    private boolean start = true;

    private Map<String, String> settingsToSet = new HashMap<>();

    public void settings() {
        size(600, 600, P3D);
        for (Settings setting : Settings.values()) {
            functionBoxes.add(new FunctionBox(functionBoxes.size(), true, setting.getName()));
        }
    }

    private int t = 0;
    public void draw() {
        rotate = keyPressed && keyCode == 18; // keyCode 18 is the ALT key
        if (draw || t++ % 60 == 0) { // Redraw the graph on update, otherwise every second
            drawGraph();
            draw = false;
        }
    }

    // Function run to make it redraw the graphs
    private void updateDraw() {
        draw = true;
    }

    private float avg(float... nums) {
        float sum = 0;
        for (float num : nums) {
            sum += num;
        }
        return sum / nums.length;
    }

    // This actually draws everything else BUT the graphs
    private void drawGraph() {
        if (start) {
            resetView();
            start = false;
        }
        lights();
        background(255);

        stroke(0);
        fill(0);
        strokeWeight(1);
        textSize(12);

        // Draw all the boxes with equations and settings, focus on the selected one
        for (FunctionBox fBox : functionBoxes) {
           fBox.draw(focusedBoxIndex == fBox.getIndex());
        }

        fill(0);
        text("Green: x-axis. Red: y-axis: Blue: z-axis.\n" +
                "Left click and drag: translate around x and z axes. Right click and drag: Translate in negative (drag up) and positive (drag down) direction on y axis.\n" +
                "Hold alt + right click and drag: Rotate around y- (up/down) and z- (left/right) axes.", 5, 20);

        // Set the correct perspective
        translate(transX, transY, transZ);
        rotateY(rotateY/200);
        rotateZ(rotateZ/200);
        scale(xScale.getValue() * 20, zScale.getValue() * 20, yScale.getValue() * 20);
        textSize(12 / avg(xScale.getValue(), yScale.getValue(), zScale.getValue()));

        // Draw the axes
        stroke(200, 0, 0);
        strokeWeight(0.1f / avg(xScale.getValue(), yScale.getValue(), zScale.getValue()));
        line(0, 0, -300, 0, 0, 300);
        stroke(0, 200, 0);
        line(-300, 0, 0, 300, 0, 0);
        stroke(0, 0, 200);
        line(0, -300, 0, 0, 300, 0);
        stroke(0);
        strokeWeight(0.05f / avg(xScale.getValue(), yScale.getValue(), zScale.getValue()));

        ArrayList<FunctionBox> toRemove = new ArrayList<>(); // Empty function boxes that we want to remove

        int index = 0;
        for (FunctionBox fBox : functionBoxes) {
            //   if the box is empty ...               and is not a setting
            if (fBox.getExpression().length() == 0 && !fBox.isSetting()) {
                toRemove.add(fBox);
                continue;
            }
            fBox.setIndex(index++);

            if (fBox.isSetting()) {
                if (focusedBoxIndex != fBox.getIndex()) {
                    // Update the text in the box, if it's not focused (don't overwrite what user is inputting)
                    fBox.setText(String.valueOf(Settings.getByName(fBox.getSettingName()).getValue()));
                }
                continue; // Don't draw setting boxes
            }

            try {
                graph(fBox);
                fBox.error("");
            } catch (MathParser.ParseException e) {
                fBox.error(e.getMessage());
            }
        }
        functionBoxes.removeAll(toRemove);
        functionBoxes.add(new FunctionBox(functionBoxes.size())); // Add 1 new empty box to the end
    }

    // Update text inside boxes when a key is typed
    public void keyTyped() {
        if (focusedBoxIndex == -1) return;
        FunctionBox fBox = functionBoxes.get(focusedBoxIndex);
        String text = fBox.getText() + key;
        fBox.setText(text);
        if (fBox.isSetting()) {
            settingsToSet.put(fBox.getSettingName(), text);
        }
        updateDraw();
    }

    // Check for the special keys
    public void keyPressed() {
        if (key == TAB) { // Tab resets the view
            resetView();
        } else if (key == ENTER) { // Enter sets the data and unfocuses the box
            focusedBoxIndex = -1;
            for (Map.Entry<String, String> set : settingsToSet.entrySet()) {
                Settings.getByName(set.getKey()).setValue(set.getValue()); // Settings are set after user is done, not on the fly, so that the program isn't trying to draw thousands of lines every time
            }
            settingsToSet = new HashMap<>();
        } else if (focusedBoxIndex != -1) {
            FunctionBox fBox = functionBoxes.get(focusedBoxIndex);
            String text = fBox.getText();
            if (key == BACKSPACE) {
                if (text.length() > 0) {
                    text = text.substring(0, text.length() - 1);
                    fBox.setText(text);
                    if (fBox.isSetting()) {
                        settingsToSet.put(fBox.getSettingName(), text);
                    }
                }
            }
        }
        updateDraw();
    }

    // Reset all view settings to their defaults
    private void resetView() {
        for (Settings setting : Settings.values()) {
            setting.reset();
        }

        transX = width/2; transY = height/2 + 100; transZ = 0;

        rotateY = 0;
        rotateZ = 0;
    }

    public void mousePressed() {
        if (mouseButton == LEFT) {
            for (FunctionBox fBox : functionBoxes) {
                if (fBox.wasClicked()) { // Set focus to a box, if it was clicked
                    focusedBoxIndex = fBox.getIndex();
                    for (Map.Entry<String, String> set : settingsToSet.entrySet()) { // Also update values for settings
                        Settings.getByName(set.getKey()).setValue(set.getValue());
                    }
                    settingsToSet = new HashMap<>();
                    return;
                }
            }

            // If not a box, set the prevMouseX and Y variables to the current position (for use in mouseDragged)
            prevMouseX = mouseX;
            prevMouseY = mouseY;
        } else if (mouseButton == RIGHT) {
            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
        updateDraw();
    }

    public void mouseDragged(MouseEvent event) {
        mouseMoved(event); // Keep all the mouse movement events in the same function
    }

    public void mouseMoved() {
        final float dX = mouseX - prevMouseX;
        final float dY = mouseY - prevMouseY;

        prevMouseX = mouseX;
        prevMouseY = mouseY;

        if (!mousePressed) return; // Make sure the mouse is down before continuing

        // Apply proper transformation and rotations
        if (mouseButton == LEFT) {
            transX += dX;
            transY += dY;
        } else if (mouseButton == RIGHT) {
            if (rotate) {
                rotateY += dX;
                rotateZ += dY;
                updateDraw();
                return;
            }
            float change = 0.5f * (sqrt(pow(dX, 2) + pow(dY, 2)));
            if (dY > 0) change *= -1;
            transZ += change;
        }
        updateDraw();
    }

    // Scroll to zoom by +/- 10%
    public void mouseWheel(MouseEvent event) {
        float e = event.getCount();
        e *= -0.1;
        e+=1;
        xScale.setValue(xScale.getValue() * e);
        yScale.setValue(yScale.getValue() * e);
        zScale.setValue(zScale.getValue() * e);
        updateDraw();
    }

    private void graph(FunctionBox fBox) throws MathParser.ParseException {
        if (xStep.getValue() <= 0 || yStep.getValue() <= 0) return;
        String exp = fBox.getExpression();
        // This key checks if the settings, including the equation are all the same. If they are, don't bother recalculating, use the old values
        String genKey = String.format("%s:%s;%s;%s;;%s;%s;%s", exp, yMin.getValue(),
                yMax.getValue(), yStep.getValue(), xMin.getValue(), xMax.getValue(), xStep.getValue());

        Map<String, Float> points;
        if (genKey.equals(fBox.getGenKey())) { // Compare the key to the previous one
            points = fBox.getPoints();
        } else { // Parse the entered equation and regenerate all the points
            points = new HashMap<>();
            MathParser parser = new MathParser();
            parser.parse(exp);
            for (float y = yMin.getValue(); y <= yMax.getValue(); y += yStep.getValue()) {
                for (float x = xMin.getValue(); x <= xMax.getValue(); x += xStep.getValue()) {
                    parser.addVariable("x", x);
                    parser.addVariable("y", y);
                    float val = -1 * parser.evaluate();
                    points.put(x + ";" + y, val);
                }
            }
            fBox.setGenKey(genKey); // Set the genKey to the new one
            fBox.setPoints(points); // Update the stored points, for use later if genKey doesn't change
        }

        for (float y = yMin.getValue(); y <= yMax.getValue(); y += yStep.getValue()) {
            float prevX = Integer.MIN_VALUE;
            float prevVal = Integer.MIN_VALUE;
            for (float x = xMin.getValue(); x <= xMax.getValue(); x += xStep.getValue()) {
                float val = points.get(x + ";" + y);
                if (prevX != Integer.MIN_VALUE && prevVal != Integer.MIN_VALUE) {
                    line(prevX, prevVal, y, x, val, y); // Plot lines, keeping y constant
                }
                prevX = x;
                prevVal = val;

                float prevY = Integer.MIN_VALUE;
                float prevVal2 = Integer.MIN_VALUE;
                for (float y2 = yMin.getValue(); y2 <= yMax.getValue(); y2 += yStep.getValue()) {
                    float val2 = points.get(x + ";" + y2);
                    if (prevY != Integer.MIN_VALUE && prevVal2 != Integer.MIN_VALUE) {
                        line(x, prevVal2, prevY, x, val2, y2); // Plot lines going perpendicular, this time keeping x constant
                    }
                    prevY = y2;
                    prevVal2 = val2;
                }
            }
        }
    }

    private static Grapher grapher;

    public static void main(String[] args) {
        String[] pArgs = { "3D Grapher" };
        grapher = new Grapher();
        PApplet.runSketch(pArgs, grapher);
    }

    public static Grapher getInstance() {
        return grapher;
    }
}
