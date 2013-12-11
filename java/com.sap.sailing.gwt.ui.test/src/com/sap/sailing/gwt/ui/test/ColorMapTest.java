package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.common.impl.ColorMapImpl;

public class ColorMapTest {

    @Test
    public void testHundredDistinctColors() {
        ColorMapImpl<Integer> colorMap = new ColorMapImpl<Integer>();
        List<String> existingColors = new ArrayList<String>();
        int amountOfDistinctColorsToCreate = 100;
        for (int i = 1; i <= amountOfDistinctColorsToCreate; i++) {
            String colorByID = colorMap.getColorByID(i).getAsHtml();
            if (!existingColors.contains(colorByID)) {
                existingColors.add(colorByID);
            }
        }
        assertEquals(amountOfDistinctColorsToCreate, existingColors.size());
    }

    /**
     * A function only useful for visual tests.
     * It creates an HTML file (as string) with 100 distinct colors on top of the water color of the google map.
     */
    public void createColorMapAsHtml() {
        ColorMapImpl<Integer> colorMap = new ColorMapImpl<Integer>();
        int amountOfDistinctColorsToCreate = 100;

        String colorMapAsHtml = "<html><head></head><body style='background-color: #A5BFDD'>";       

        for(int i = 1; i <= amountOfDistinctColorsToCreate; i++) {
            colorMapAsHtml += "<div style='height:3px; background-color:" + colorMap.getColorByID(i) + "'></div><br/>";
        }

        colorMapAsHtml += "</body></html>";
        System.out.println(colorMapAsHtml);
    }
}
