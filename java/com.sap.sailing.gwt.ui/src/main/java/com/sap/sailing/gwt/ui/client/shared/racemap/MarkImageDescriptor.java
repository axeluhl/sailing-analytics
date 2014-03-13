package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.Point;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.domain.common.MarkType;

/**
 * A descriptor class for display properties of a mark image.
 */
public class MarkImageDescriptor {
    private final ImageResource imgageResource;
    private final String color;
    private final String shape;
    private final String pattern;
    private final MarkType type;
    private final Point anchorPoint;

    public MarkImageDescriptor(final ImageResource imgageResource, final Point anchorPoint, MarkType type, String color, String shape, String pattern) {
        this.imgageResource = imgageResource;
        this.anchorPoint = anchorPoint;
        this.type = type;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
    }

    public ImageResource getImgageResource() {
        return imgageResource;
    }

    public Point getAnchorPoint() {
        return anchorPoint;
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
            isSameColor = color.equalsIgnoreCase(colorToCheck) ? true : false;
        }
        if(shapeToCheck != null && shape != null) {
            isSameShape = shape.equalsIgnoreCase(shapeToCheck) ? true : false;
        }
        if(patternToCheck != null && pattern != null) {
            isSamePattern = pattern.equalsIgnoreCase(patternToCheck) ? true : false;
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
