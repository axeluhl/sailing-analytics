package com.sap.sailing.domain.common.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sap.sailing.domain.common.ColorMap;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.HSVColor;


/**
 * Manages color assignments to objects.
 * 
 * @author Axel Uhl (d043530)
 * 
 * @param <T>
 *            the type of the objects to which a color is assigned
 */
public class ColorMapImpl<T> implements ColorMap<T> {
    private final HashMap<T, Color> idColor;

    private int colorCounter;

    private HSVColor[] baseColors;

    /** a list of already used colors which should be excluded from the automatic color assignment */
    private List<HSVColor> blockedColors;
    
    public ColorMapImpl() {
        baseColors = new HSVColor[10];
        baseColors[0] = new HSVColor(0.0f, 1.0f, 1.0f); // Red
        baseColors[1] = new HSVColor(30.0f, 1.0f, 1.0f); // Orange
        baseColors[2] = new HSVColor(45.f, 1.0f, 1.0f);
        baseColors[3] = new HSVColor(120.0f, 1.0f, 0.8f); // Green
        baseColors[4] = new HSVColor(200.0f, 1.0f, 1.0f); // Cyan
        baseColors[5] = new HSVColor(240.0f, 1.0f, 1.0f); // Blue
        baseColors[6] = new HSVColor(270.0f, 1.0f, 1.0f); // Pink
        baseColors[7] = new HSVColor(285.0f, 1.0f, 1.0f); 
        baseColors[8] = new HSVColor(300.0f, 1.0f, 1.0f); // Magenta
        baseColors[9] = new HSVColor(330.0f, 1.0f, 1.0f); 

        idColor = new HashMap<T, Color>();
        blockedColors = new ArrayList<HSVColor>();
    }

    /**
     * Returns a color that is computed once.
     * 
     * @param object
     *            An ID unique for something this map is to provide a color for. Color assignment is based on the
     *            object's {@link Object#equals(Object)} and {@link Object#hashCode()}.
     * @return A color in hex/html-format (e.g. #ff0000)
     */
    public Color getColorByID(T object) {
        Color color = idColor.get(object);
        if (color == null) {
            color = createHexColor(colorCounter++);
            idColor.put(object, color);
        }
        return color;
    }

    public boolean addBlockedColor(Color color) {
        boolean result = false;
        if(color != null) {
            Util.Triple<Float, Float, Float> asHSV = color.getAsHSV();
            result = blockedColors.add(new HSVColor(asHSV.getA(), asHSV.getB(), asHSV.getC()));
        }
        return result; 
    }
    
    public boolean removeBlockedColor(Color color) {
        boolean result = false;
        if(color != null) {
            Util.Triple<Float, Float, Float> asHSV = color.getAsHSV();
            result = blockedColors.remove(new HSVColor(asHSV.getA(), asHSV.getB(), asHSV.getC()));
        }
        return result;
    }
    
    public void clearBlockedColors() {
        blockedColors.clear();
    }

    /**
     * Only use this if you don't want the color to be cached.
     * 
     * @param index
     *            The index of e.g. a competitor. Make sure, that each competitor has a unique index.
     * @return A color computed using the {@code index}.
     */
    private Color createHexColor(int index) {
        int baseColorCount = baseColors.length;
        int baseColorsIndex = index % baseColorCount;
        int factor = index / (baseColorCount*3);
        float decreaseStepSize = 0.075f;
        
        float brightnessDecrease = 0;
        float saturationDecrease = 0;
        int mod3 = index % 3;
        switch(mod3) {
        case 0:
        	brightnessDecrease = 0.0f;
        	saturationDecrease = 0.0f;
        	break;
        case 1:
        	brightnessDecrease = decreaseStepSize;
        	saturationDecrease = 0.0f;
        	break;
        case 2:
        	brightnessDecrease = 0.0f;
        	saturationDecrease = decreaseStepSize;
        	break;
        }
        brightnessDecrease = factor * decreaseStepSize + brightnessDecrease;
        saturationDecrease = factor * decreaseStepSize + saturationDecrease;
        HSVColor hsvColor = baseColors[baseColorsIndex];
        HSVColor newColor = new HSVColor(hsvColor.getHue(), hsvColor.getSaturation() - saturationDecrease,
                hsvColor.getBrightness() - brightnessDecrease);
        return newColor;
    }
}
