package com.pantherman594;

public enum Settings {
    xMin("x min", "-3"),
    xMax("x max", "3"),
    xStep("x step", "0.25", true),
    xScale("x scale", "1", true),
    yMin("y min", "-3"),
    yMax("y max", "3"),
    yStep("y step", "0.25", true),
    yScale("y scale", "1", true),
    zScale("z scale", "1", true);

    private String name;
    private String defaultValue;
    private String value;
    private boolean alwaysPositive = false;

    Settings(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        value = defaultValue;
    }

    Settings(String name, String defaultValue, boolean alwaysPositive) {
        this.name = name;
        this.defaultValue = defaultValue;
        value = defaultValue;
        this.alwaysPositive = true;
    }

    public String getName() {
        return name;
    }

    public float getValue() {
        if (value.endsWith(".")) value += "0";
        float value;
        try {
            value = Float.parseFloat(this.value);
        } catch (NumberFormatException e) {
            return getDefaultValue();
        }
        if (alwaysPositive && value < 0) return getDefaultValue();
        return value;
    }

    public void setValue(String value) throws NumberFormatException {
        this.value = value;
    }

    public void setValue(float value) {
        setValue(String.valueOf(value));
    }

    public float getDefaultValue() {
        return Float.parseFloat(defaultValue);
    }

    public void reset() {
        value = defaultValue;
    }

    public static Settings getByName(String name) {
        for (Settings setting : Settings.values()) {
            if (setting.getName().equals(name)) return setting;
        }
        System.out.println("NULL: " + name);
        return null;
    }
}
