package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.Util.Triple;

/**
 * A color defined in the RGB color schema
 * @author Frank
 */
public class RGBColor implements Color {
    private static final long serialVersionUID = -4091876840771631308L;
    private int red;
    private int green;
    private int blue;

    RGBColor() {
    } // for GWT serializability

    public RGBColor(int red, int green, int blue) {
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

    public Triple<Float, Float, Float> getAsHSV() {
        float hue, saturation, brightness;
        int cmax = (red > green) ? red : green;
        if (blue > cmax) {
            cmax = blue;
        }
        int cmin = (red < green) ? red : green;
        if (blue < cmin) {
            cmin = blue;
        }

        brightness = ((float) cmax) / 255.0f;
        if (cmax != 0) {
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        } else {
            saturation = 0;
        }
        if (saturation == 0) {
            hue = 0;
        }
        else {
            float redc = ((float) (cmax - red)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - green)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - blue)) / ((float) (cmax - cmin));
            if (red == cmax) {
                hue = bluec - greenc;
            } else if (green == cmax) {
                hue = 2.0f + redc - bluec;
            } else {
                hue = 4.0f + greenc - redc;
            }
            hue = hue / 6.0f;
            if (hue < 0) {
                hue = hue + 1.0f;
            }
        }

        Triple<Float, Float, Float> HSVColor = new Triple<Float, Float, Float>(hue, saturation, brightness);
        return HSVColor;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + blue;
        result = prime * result + green;
        result = prime * result + red;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RGBColor other = (RGBColor) obj;
        if (blue != other.blue)
            return false;
        if (green != other.green)
            return false;
        if (red != other.red)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getAsHtml();
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }
}
