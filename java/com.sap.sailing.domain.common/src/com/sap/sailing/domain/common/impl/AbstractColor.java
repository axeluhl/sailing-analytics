package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Color;
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
}
