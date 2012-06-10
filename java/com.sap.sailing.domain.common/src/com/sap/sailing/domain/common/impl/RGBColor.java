package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.Util.Triple;

public class RGBColor implements Color {
    private static final long serialVersionUID = -4091876840771631308L;
    private int red;
    private int green;
    private int blue;
    
    RGBColor() {} // for GWT serializability
    
    public RGBColor(int red, int green, int blue) {
        super();
        this.red = ensureValidRange(red);
        this.green = ensureValidRange(green);
        this.blue = ensureValidRange(blue);
    }

    private int ensureValidRange(int value) {
        int result = value;
        if (value < 0) {
            result = 0;
        } else if (value > 255) {
            result = 255;
        }
        return result;
    }

    @Override
    public Triple<Integer, Integer, Integer> getAsRGB() {
        return new Triple<Integer, Integer, Integer>(red, green, blue);
    }

    @Override
    public String getAsHtml() {
        return "#" + toBrowserHexValue(red) + toBrowserHexValue(green) + toBrowserHexValue(blue);
    }
    
    private static String toBrowserHexValue(int number) {
        StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
        while (builder.length() < 2) {
            builder.append("0");
        }
        return builder.toString().toUpperCase();
    }

}
