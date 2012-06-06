package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.RGBColor;

public enum GroupColors {
    GOLD (255, 215, 0), 
    SILVER (201, 192, 187),
    BRONZE (205, 127, 50), 
    EMERALD (0, 201, 87),
    BLUE (0, 0, 255),
    YELLOW (255,255,0),
    GREEN (0, 255, 0),
    RED (255, 0, 0);
    
    private Color color;
    
    GroupColors(int red, int green, int blue) {
        color = new RGBColor(red, green, blue);
    }

    public Color getColor() {
        return color;
    }
}
