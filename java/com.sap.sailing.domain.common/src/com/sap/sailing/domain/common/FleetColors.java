package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.impl.RGBColor;

public enum FleetColors {
    GOLD (222, 138, 46, 1), 
    SILVER (192, 192, 192, 2),
    BRONZE (140, 120, 83, 3), 
    EMERALD (109, 201, 59, 4),
    BLUE (39, 140, 194, 0),
    YELLOW (251,186,0, 0),
    GREEN (138, 181, 78, 0),
    RED (232, 74, 26, 0);
    
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
