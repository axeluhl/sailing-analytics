package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.*;

import org.junit.Test;
import com.sap.sailing.gwt.ui.simulator.util.*;

public class WindGridColorPaletteTest {

    @Test
    public void test() {
        int min = 10;
        int max = 100;
        WindGridColorPalette cp = new WindGridColorPalette(min,max);
        
        assertEquals("Lightest color", "#FFFFFF", cp.getColor(min));
        assertEquals("Darkest color", "#000000", cp.getColor(max));
        
        assertEquals("In between color", "#7F7F7F", cp.getColor((max+min)/2));
    }

}
