package com.sap.sse.common.impl;

import com.sap.sse.common.Color;
import com.sap.sse.common.Util;

public abstract class AbstractColor implements Color {
    private static final long serialVersionUID = 7758884012281863458L;

    @Override
    public String getAsHtml() {
        Util.Triple<Integer, Integer, Integer> asRGB = getAsRGB();
        return "#" + toBrowserHexValue(asRGB.getA()) + toBrowserHexValue(asRGB.getB())
                + toBrowserHexValue(asRGB.getC());
    }
    
    private static String toBrowserHexValue(int number) {
        StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
        while (builder.length() < 2) {
            builder.append("0");
        }
        return builder.toString().toUpperCase();
    }

    public static Color getColorByLowercaseNameStatic(String lowercaseColorName) {
        final Color result;
        if ("white".equals(lowercaseColorName)) {
            result = WHITE;
        } else if ("light_gray".equals(lowercaseColorName)) {
            result = LIGHT_GRAY;
        } else if ("gray".equals(lowercaseColorName)) {
            result = GRAY;
        } else if ("dark_gray".equals(lowercaseColorName)) {
            result = DARK_GRAY;
        } else if ("black".equals(lowercaseColorName)) {
            result = BLACK;
        } else if ("red".equals(lowercaseColorName)) {
            result = RED;
        } else if ("pink".equals(lowercaseColorName)) {
            result = PINK;
        } else if ("orange".equals(lowercaseColorName)) {
            result = ORANGE;
        } else if ("yellow".equals(lowercaseColorName)) {
            result = YELLOW;
        } else if ("green".equals(lowercaseColorName)) {
            result = GREEN;
        } else if ("magenta".equals(lowercaseColorName)) {
            result = MAGENTA;
        } else if ("cyan".equals(lowercaseColorName)) {
            result = CYAN;
        } else if ("blue".equals(lowercaseColorName)) {
            result = BLUE;
        } else {
            result = null;
        }
        return result;
    }
}
