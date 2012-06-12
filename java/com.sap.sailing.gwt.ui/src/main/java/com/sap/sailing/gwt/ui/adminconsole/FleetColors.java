package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.RGBColor;

public enum FleetColors {
    GOLD (255, 215, 0, 1), 
    SILVER (201, 192, 187, 2),
    BRONZE (205, 127, 50, 3), 
    EMERALD (0, 201, 87, 4),
    BLUE (0, 0, 255, 0),
    YELLOW (255,255,0, 0),
    GREEN (0, 255, 0, 0),
    RED (255, 0, 0, 0);
    
    private Color color;
    
    private int defaultOrderNo;

    FleetColors(int red, int green, int blue, int defaultOrderNo) {
        color = new RGBColor(red, green, blue);
        this.defaultOrderNo = defaultOrderNo;
    }

    public Color getColor() {
        return color;
    }

    public int getDefaultOrderNo() {
        return defaultOrderNo;
    }
}
