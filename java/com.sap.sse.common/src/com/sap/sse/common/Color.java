package com.sap.sse.common;

import java.io.Serializable;

import com.sap.sse.common.impl.RGBColor;

public interface Color extends Serializable {
    final Color WHITE = new RGBColor(255, 255, 255);

    final Color LIGHT_GRAY = new RGBColor(192, 192, 192);

    final Color GRAY = new RGBColor(128, 128, 128);

    final Color DARK_GRAY = new RGBColor(64, 64, 64);

    final Color BLACK = new RGBColor(0, 0, 0);

    final Color RED = new RGBColor(255, 0, 0);

    final Color PINK = new RGBColor(255, 175, 175);

    final Color ORANGE = new RGBColor(255, 200, 0);

    final Color YELLOW = new RGBColor(255, 255, 0);

    final Color GREEN = new RGBColor(0, 255, 0);

    final Color MAGENTA = new RGBColor(255, 0, 255);

    final Color CYAN = new RGBColor(0, 255, 255);

    final Color BLUE = new RGBColor(0, 0, 255);
    
    com.sap.sse.common.Util.Triple<Integer, Integer, Integer> getAsRGB();

    com.sap.sse.common.Util.Triple<Float, Float, Float> getAsHSV();

    String getAsHtml();
}
