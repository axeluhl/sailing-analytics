package com.sap.sailing.gwt.ui.simulator.util;

/**
 * A class to implement a 2d point to support the capture of area around the point for displaying tooltips.
 * The co-ordinates are in pixels.
 * 
 * @author Nidhi Sawhney(D054070)
 */
public class ToolTip {

    private double x;
    private double y;
    
    /**
     * The tolerance in pixels around the point where the tool tip is to be displayed 
     */
    static final int toolTipTolerance = 50;
    
    /**
     * The dimensions of the tool tip to be displayed
     */
    public static final double toolRectW = 80;
    public static final double toolRectH = 30;
    
    /**
     * 
     * @param x coordinate value in pixels
     * @param y coordinate value in pixels
     */
    public ToolTip(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ToolTip other = (ToolTip) obj;
        if (other.x <= x + toolTipTolerance && other.x >= x - toolTipTolerance 
                && other.y <= y + toolTipTolerance && other.y >= y - toolTipTolerance)
            return true;
      
        return false;
    }

    @Override
    public int hashCode() {
       return (int)x + (int)y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
    
}
