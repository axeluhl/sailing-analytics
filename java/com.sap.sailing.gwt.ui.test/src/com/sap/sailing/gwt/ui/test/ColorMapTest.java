package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.gwt.ui.client.ColorMap;

public class ColorMapTest {

    @Test
    public void testHundredDistinctColors() {
        ColorMap<Integer> colorMap = new ColorMap<Integer>();
        List<String> existingColors = new ArrayList<String>(); 
        int amountOfDistinctColorsToCreate = 100;
        for(int i = 1; i <= amountOfDistinctColorsToCreate; i++) {
            String colorByID = colorMap.getColorByID(i);
            if(!existingColors.contains(colorByID)) {
                existingColors.add(colorByID);
            }
        }
        assertEquals(amountOfDistinctColorsToCreate, existingColors.size());
    }

}
