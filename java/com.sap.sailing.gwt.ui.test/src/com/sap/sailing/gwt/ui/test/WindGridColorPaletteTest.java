package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.gwt.ui.simulator.util.WindGridColorPalette;

public class WindGridColorPaletteTest {

    @Test
    public void test() {
        int min = 10;
        int max = 100;
        WindGridColorPalette cp = new WindGridColorPalette(min,max);
        
        //assertEquals("Lightest color", "#FFFFFF", cp.getColor(min));
        //assertEquals("Darkest color", "#0000FF", cp.getColor(max));
        
        //assertEquals("In between color", "#7F7FFF", cp.getColor((max+min)/2));
       
        assertEquals("Lightest color", "#FFFFFF", cp.getColor(min));
        assertEquals("Darkest color", "#4B7FBB", cp.getColor(max));
        
        assertEquals("In between color", "#A5BFDD", cp.getColor((max+min)/2));
    }

}
