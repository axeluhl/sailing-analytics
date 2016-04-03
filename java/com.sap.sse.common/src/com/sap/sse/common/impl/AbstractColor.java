package com.sap.sse.common.impl;

import java.lang.reflect.Field;
import java.util.Map;

import com.google.gwt.dev.util.collect.HashMap;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;

public abstract class AbstractColor implements Color {
    private static final long serialVersionUID = 7758884012281863458L;

    private static final Map<String,Color> colorNameToColor = createColorMap();
    
    @Override
    public String getAsHtml() {
        Util.Triple<Integer, Integer, Integer> asRGB = getAsRGB();
        return "#" + toBrowserHexValue(asRGB.getA()) + toBrowserHexValue(asRGB.getB())
                + toBrowserHexValue(asRGB.getC());
    }
    
    private static Map<String, Color> createColorMap() {
        HashMap<String,Color> map = new HashMap<String, Color>();
        String name;
        for (Field field : Color.class.getFields()) {
            if (field.getClass().isAssignableFrom(Color.class)) {
                name = field.getName().toLowerCase().replace("_", "");
                try {
                    map.put(name, (Color) field.get(null));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // ignore this case, and don't add something to the map.
                }
            }
        }
        return map;
    }

    private static String toBrowserHexValue(int number) {
        StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
        while (builder.length() < 2) {
            builder.append("0");
        }
        return builder.toString().toUpperCase();
    }

    public static Color getColorByLowercaseNameStatic(String lowercaseColorName) {
        return colorNameToColor.get(lowercaseColorName);
    }
}
