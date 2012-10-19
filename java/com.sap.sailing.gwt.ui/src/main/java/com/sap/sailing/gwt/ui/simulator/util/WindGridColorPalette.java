package com.sap.sailing.gwt.ui.simulator.util;

/**
 * Class to provide a color for a given double value, much like in a heat map.
 * The heat map is created over the range of minColor and maxColor.
 * Currently this implementation only provides shades of grey hence RGB have the same
 * value.
 */
public class WindGridColorPalette implements ColorPalette {

    /*White is 255 or FF */
    public int minColor = 255; 
    /*Black is 0 or 00 */
    public int maxColor = 0;
    
    private double min;
    private double max;
    
    /**
     * Provide the range of values for which the colors are to be generated
     * @param min
     * @param max
     */
    public WindGridColorPalette(double min, double max) {
        this.min = min;
        this.max = max;
    }
    
    @Override
    public String getNextColor() {
       throw new UnsupportedOperationException();
    }

    @Override
    public String getColor(int idx) {
       return getColor(idx*1.0);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * 
     * @param value
     * @return a color representation in hex prefixed with a # corresponding to the value
     * Currently only shades of grea are supported hence RGB have the same values
     * if the value is in th range [min,max], returns null otherwise
     */
    public String getColor(double value) {
        if (min <= value && value <= max) {
            
            int colorValue = (int)(minColor + (value-min)/Math.max(1,(max - min)) * (maxColor - minColor));
            String colorValueHex = Integer.toHexString(colorValue).toUpperCase();
            if (colorValueHex.length() < 2) {
                colorValueHex = "0" + colorValueHex;
            }
            String rgb = "#" + colorValueHex + colorValueHex + colorValueHex;
            return rgb;
        }
        return null;
    }

}
