package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.Util.Triple;

/**
 * A color defined in the HSV color schema (hue, saturation, brightness) Hue is a degree between 0.0 and 360.0
 * Saturation is between 0.0 and 1.0 Brightness is between 0.0 and 1.0
 * 
 * @author Frank
 */
public class HSVColor implements Color {
    private static final long serialVersionUID = 7602013229606352246L;

    float hue;
    float saturation;
    float brightness;

    HSVColor() {
    } // for GWT serializability

    public HSVColor(float hue, float saturation, float brightness) {
        this.hue = ensureValidRange(hue, 360.0f);
        this.saturation = ensureValidRange(saturation, 1.0f);
        this.brightness = ensureValidRange(brightness, 1.0f);
    }

    private float ensureValidRange(float value, float maxValue) {
        float result = value;
        if (value < 0.0f) {
            result = 0.0f;
        } else if (value > maxValue) {
            result = maxValue;
        }
        return result;
    }

    @Override
    public Triple<Integer, Integer, Integer> getAsRGB() {
        float r, b, g;

        if (saturation == 0) {
            r = g = b = brightness;
        } else {
            float h = hue / 60.0f; // sector 0 to 5
            int i = (int) Math.floor(h);
            float f = h - i; // factorial part of h
            float p = brightness * (1 - saturation);
            float q = brightness * (1 - saturation * f);
            float t = brightness * (1 - saturation * (1 - f));

            switch (i) {
            case 0:
                r = brightness;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = brightness;
                b = p;
                break;
            case 2:
                r = p;
                g = brightness;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = brightness;
                break;
            case 4:
                r = t;
                g = p;
                b = brightness;
                break;
            default: // case 5:
                r = brightness;
                g = p;
                b = q;
            }
        }
        Triple<Integer, Integer, Integer> RGBColor = new Triple<Integer, Integer, Integer>(Math.round(r * 255),
                Math.round(g * 255), Math.round(b * 255));
        return RGBColor;
    }

    @Override
    public String getAsHtml() {
        Triple<Integer, Integer, Integer> asRGB = getAsRGB();
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

    @Override
    public Triple<Float, Float, Float> getAsHSV() {
        return new Triple<Float, Float, Float>(hue, saturation, brightness);
    }

    @Override
    public String toString() {
        return "HSVColor [hue=" + hue + ", saturation=" + saturation + ", brightness=" + brightness + "]";
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getBrightness() {
        return brightness;
    }
}
