package com.sap.sailing.gwt.ui.simulator.util;

/**
 * Class to provide a color for a given double value, much like in a heat map.
 * The heat map is created over the range of minColor and maxColor.
 * Currently this implementation only provides shades of grey hence RGB have the same
 * value.
 */
public class WindGridColorPalette implements ColorPalette {

    /*White is 255 or FF */
    public int minColor = 255;//200; 
    /*Black is 0 or 00 */
    public int maxColor = 0;//100;
    
    public int rminColor = 255;
    public int rmaxColor = 75;
    public int gminColor = 255;
    public int gmaxColor = 127;
    public int bminColor = 255;
    public int bmaxColor = 187;
    
    
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
     * a shade of grey is returned hence RGB have the same values
     * if the value is in the range [min,max], returns null otherwise
     */
    public String getGreyColor(double value) {
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

    /**
     * 
     * @param value
     * @return a color representation in hex prefixed with a # corresponding to the value
     * The base shade is Google map's ocean blue with shading depending on the value
     * if the value is in the range [min,max], returns null otherwise
     */
    public String getColor(double value) {
        if (min <= value && value <= max) {
            int rbase = (rminColor + rmaxColor)/2;
            int gbase = (gminColor + gmaxColor)/2;
            int bbase = (bminColor + bmaxColor)/2;
            double avgValue = (max+min)/2.0;
            int rColorValue;
            int gColorValue;
            int bColorValue;
            if (value <= avgValue) {
                
                rColorValue = (int)(rbase - (avgValue - value)/Math.max(1,(avgValue - min)) * (rbase - rminColor));
                gColorValue = (int)(gbase - (avgValue - value)/Math.max(1,(avgValue - min)) * (gbase - gminColor));
                bColorValue = (int)(bbase - (avgValue -value)/Math.max(1,(avgValue - min)) * (bbase - bminColor));
           
            } else {
                rColorValue = (int)(rbase + (value - avgValue)/Math.max(1,(max - avgValue)) * (rmaxColor - rbase));
                gColorValue = (int)(gbase + (value - avgValue)/Math.max(1,(max - avgValue)) * (gmaxColor - gbase));
                bColorValue = (int)(bbase + (value - avgValue)/Math.max(1,(max - avgValue)) * (bmaxColor - bbase));
                
            }
            
          
            String rcolorValueHex = Integer.toHexString(rColorValue).toUpperCase();
            if (rcolorValueHex.length() < 2) {
                rcolorValueHex = "0" + rcolorValueHex;
            }
            String gcolorValueHex = Integer.toHexString(gColorValue).toUpperCase();
            if (gcolorValueHex.length() < 2) {
                gcolorValueHex = "0" + gcolorValueHex;
            }
            String bcolorValueHex = Integer.toHexString(bColorValue).toUpperCase();
             if (bcolorValueHex.length() < 2) {
                 bcolorValueHex = "0" + bcolorValueHex;
             }
             String rgb = "#" + rcolorValueHex + gcolorValueHex + bcolorValueHex;
             return rgb;
       
        }
        return null;
    }
}
