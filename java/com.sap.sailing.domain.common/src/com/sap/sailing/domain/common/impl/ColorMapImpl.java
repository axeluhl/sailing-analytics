package com.sap.sailing.domain.common.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sap.sailing.domain.common.ColorMap;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util.Triple;
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
    
    /**
     * Used for blocking a range of colors around each color in {@link ColorMapImpl#blockedColors}
     */
    private static final double MIN_COLOR_DISTANCE = 0.5;
    
    private static final float STEP = 0.1f;
    /*
     * Number of steps were made
     */
    private static int stepsMade = 0;
    /**
     * <p>
     * There are 3 modes of color generation.
     * </p>
     * <ul>
     * <li>Mode "0" is used for generating colors with identical brightness and saturation.</li>
     * <li>Mode "1" is used for generating colors with different saturation and the constant brightness</li>
     * <li>Mode "2" is opposite of the mode "1"</li>
     * </ul>
     */
    private static int mode = 0;
    
    /**
     * Changes from 0 to line {@link ColorMapImpl#stepsMade} cyclically
     */
    private static int factor = 0;

    /** a list of already used colors which should be excluded from the automatic color assignment */
    private List<HSVColor> blockedColors;

    public ColorMapImpl(Color... initialBlockedColors) {
        baseColors = insertBaseColors();
        idColor = new HashMap<>();
        blockedColors = new ArrayList<>();
        for (Color initialBlockedColor : initialBlockedColors) {
            blockedColors.add(convertFromColorToHSV(initialBlockedColor));
        }
    }

    private HSVColor[] insertBaseColors() {
        final HSVColor[] result = new HSVColor[] {
            new HSVColor(0,   1, 1), // Red
            new HSVColor(30,  1, 1), // Orange
            new HSVColor(45,  1, 1),
            new HSVColor(120, 1, 1), // Green
            new HSVColor(160, 1, 1),
            new HSVColor(190, 1, 1),
            new HSVColor(240, 1, 1), // Blue
            new HSVColor(270, 1, 1), // Pink
            new HSVColor(285, 1, 1),
            new HSVColor(300, 1, 1), // Magenta
            new HSVColor(330, 1, 1) };
        return result;
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
        final Color result;
        Color color = idColor.get(object);
        if (color == null) {
            HSVColor newColor;
            do {
                newColor = generateColor(colorCounter++);
            } while (isContainColor(blockedColors, newColor));
            idColor.put(object, newColor);
            result = newColor;
        } else {
            result = color;
        }
        return result;
    }
    
    private boolean isColorsClose(HSVColor blockedColor, HSVColor newColor) {
        double distanceHue = Math.abs(blockedColor.getHue() - newColor.getHue());
        distanceHue = Math.min(distanceHue, 360 - distanceHue) / 180;
        double distanceSaturation = Math.abs(blockedColor.getSaturation() - newColor.getSaturation());
        double distanceBrightness = Math.abs(blockedColor.getBrightness() - newColor.getBrightness());
        double distance = Math.sqrt(distanceHue * distanceHue + distanceSaturation * distanceSaturation
                + distanceBrightness * distanceBrightness);
        return distance < MIN_COLOR_DISTANCE;
    }
    
    private boolean isContainColor(List<HSVColor> colors, HSVColor color) {
        for (HSVColor colorFromColors : colors) {
            if (isColorsClose(colorFromColors, color)) {
                return true;
            }
        }
        return false;
    }

    public boolean addBlockedColor(Color color) {
        final boolean result;
        if (color != null && !blockedColors.contains(color)) {
            result = blockedColors.add(convertFromColorToHSV(color));
        } else {
            result = false;
        }
        return result;
    }
    
    public boolean removeBlockedColor(Color color) {
        boolean result = false;
        if (color != null) {
            result = blockedColors.remove(convertFromColorToHSV(color));
        }
        return result;
    }
    
    public void clearBlockedColors() {
        blockedColors.clear();
    }
    
    private static HSVColor convertFromColorToHSV(Color color) {
        Triple<Float, Float, Float> hsvColor = color.getAsHSV();
        return new HSVColor(hsvColor.getA(), hsvColor.getB(), hsvColor.getC());
    }

    /**
     * Generator uses cyclic algorithm for generating colors. We have the array {@link ColorMapImpl#baseColors} and goes
     * through it cyclically. When a cycle starts from the beginning again then increase {@link ColorMapImpl#stepsMade}
     * that is used for getting new shade of basic colors and combining it with the previous ones.
     * 
     * Only use this if you don't want the color to be cached.
     * 
     * @param index
     *            The index of e.g. a competitor. Make sure, that each competitor has a unique index.
     * @return A color computed using the {@code index}.
     * @author Stsiapan_Tsybulski
     */
    private HSVColor generateColor(int index) {
        int baseColorsCount = baseColors.length;
        int currentColorIndex = index % baseColorsCount;
        float saturationDecrease = STEP;
        float brightnessDecrease = STEP;
        switch (mode) {
        case 0:
            saturationDecrease = brightnessDecrease *= stepsMade;
            break;
        case 1:
            saturationDecrease *= factor;
            brightnessDecrease *= stepsMade;
            break;
        case 2:
            brightnessDecrease *= factor;
            saturationDecrease *= stepsMade;
            break;
        default:
            break;
        }
        if ((index + 1) % baseColorsCount == 0) {
            factor++;
            switch (mode) {
            case 0:
                stepsMade++;
                mode = 1;
                factor = 0;
                break;
            case 1:
                if (factor == stepsMade) {
                    mode = 2;
                    factor = 0;
                }
                break;
            case 2:
                if (factor == stepsMade) {
                    mode = 0;
                }
                break;
            default:
                break;
            }
        }
        HSVColor hsvColor = baseColors[currentColorIndex];
        return new HSVColor(hsvColor.getHue(), hsvColor.getSaturation() - saturationDecrease,
                hsvColor.getBrightness() - brightnessDecrease);
    }
}
