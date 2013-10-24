package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.maps.client.overlays.MarkerImage;
import com.sap.sailing.domain.common.MarkType;

/**
 * A descriptor class for display properties of a mark icon.
 */
public class MarkIconDescriptor {
    private final MarkerImage markerImage;
    private final String color;
    private final String shape;
    private final String pattern;
    private final MarkType type;

    public MarkIconDescriptor(MarkerImage markerImage, MarkType type, String color, String shape, String pattern) {
        this.markerImage = markerImage;
        this.type = type;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
    }

    public MarkerImage getMarkerImage() {
        return markerImage;
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
     * In case of null values at least one valid display property must exist and fit.  
     */
    public boolean isCompatible(MarkType typeToCheck, String colorToCheck, String shapeToCheck, String patternToCheck) {
        boolean result = false;

        if((typeToCheck != null && type == null) || (typeToCheck == null && type != null)) {
            result = false;
        } else if(typeToCheck != null && type != null) {
            result = type == typeToCheck;
        }
        
        if((colorToCheck != null && color == null) || (colorToCheck == null && color != null)) {
            result = false;
        } else if(colorToCheck != null && color != null) {
            result = color.equalsIgnoreCase(colorToCheck) ? true : false;
        }

        if((shapeToCheck != null && shape == null) || (shapeToCheck == null && shape != null)) {
            result = false;
        } else if(shapeToCheck != null && shape != null) {
            result = shape.equalsIgnoreCase(shapeToCheck) ? true : false;
        }

        if((patternToCheck != null && pattern == null) || (patternToCheck == null && pattern != null)) {
            result = false;
        } else if(patternToCheck != null && pattern != null) {
            result = pattern.equalsIgnoreCase(patternToCheck) ? true : false;
        }   
        
        return result;    
    }

}
