package com.sap.sailing.racecommittee.app.ui.utils;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.AbstractColor;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.DrawableRes;

public class MarkImageDescriptor {

    private final int drawableId;
    private final Color color;
    private final String shape;
    private final String pattern;
    private final MarkType type;
    private final LayerDrawable drawable;

    /**
     * @param color
     *            this color string, with {@code buoy_color_} and {@code buoy_outline_} prepended, is matched against
     *            the color constants defined in {@code res/values/colors.xml}. If not matched, the default constants
     *            {@code buoy_color} and {@code buoy_online} from {@code res/values/colors.xml} are used.
     */
    public MarkImageDescriptor(Context context, int drawableId, MarkType type, String color, String shape, String pattern) {
        this.drawableId = drawableId;
        this.type = type;
        this.color = AbstractColor.getCssColor(color);
        this.shape = shape;
        this.pattern = pattern;
        this.drawable = BuoyHelper.getBuoy(context, type, color, shape, pattern);
    }

    public
    @DrawableRes
    int getDrawableId() {
        return drawableId;
    }

    public LayerDrawable getDrawable() {
        return drawable;
    }

    public String getShape() {
        return shape;
    }

    public String getPattern() {
        return pattern;
    }

    public MarkType getType() {
        return type;
    }

    /**
     * An utility method to check if the mark icon has compatible display properties.
     * In case of null values at least the mark type and the color must exist and fit.
     */
    public int getCompatibilityLevel(MarkType typeToCheck, Color colorToCheck, String shapeToCheck, String patternToCheck) {
        boolean isSameType = false;
        boolean isSameColor = false;
        boolean isSameShape = false;
        boolean isSamePattern = false;
        int result = -1;
        if (typeToCheck != null && type != null) {
            isSameType = type == typeToCheck;
        }
        if (colorToCheck != null && color != null) {
            isSameColor = color.equals(colorToCheck);
        }
        if (shapeToCheck != null && shape != null) {
            isSameShape = shape.equalsIgnoreCase(shapeToCheck);
        }
        if (patternToCheck != null && pattern != null) {
            isSamePattern = pattern.equalsIgnoreCase(patternToCheck);
        }
        if (isSameType && isSameColor) {
            result = 1;
            if (isSameShape) {
                result += 1;
            }
            if (isSamePattern) {
                result += 1;
            }
        }
        return result;
    }
}
