package com.sap.sailing.racecommittee.app.ui.utils;

import com.sap.sailing.domain.common.MarkType;

public class MarkImageDescriptor {

    private final int drawableId;
    private final String color;
    private final String shape;
    private final String pattern;
    private final MarkType type;

    public MarkImageDescriptor(int drawableId, MarkType type, String color, String shape, String pattern) {
        this.drawableId = drawableId;
        this.type = type;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public String getColor() {
        return color;
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
    public int getCompatibilityLevel(MarkType typeToCheck, String colorToCheck, String shapeToCheck, String patternToCheck) {
        boolean isSameType = false;
        boolean isSameColor = false;
        boolean isSameShape = false;
        boolean isSamePattern = false;
        int result = -1;

        if(typeToCheck != null && type != null) {
            isSameType = type == typeToCheck;
        }
        if(colorToCheck != null && color != null) {
            isSameColor = color.equalsIgnoreCase(colorToCheck);
        }
        if(shapeToCheck != null && shape != null) {
            isSameShape = shape.equalsIgnoreCase(shapeToCheck);
        }
        if(patternToCheck != null && pattern != null) {
            isSamePattern = pattern.equalsIgnoreCase(patternToCheck);
        }   

        if(isSameType && isSameColor) {
            result = 1;
            if(isSameShape) {
                result += 1;
            }
            if(isSamePattern) {
                result += 1;
            }
        }

        return result;    
    }
}
