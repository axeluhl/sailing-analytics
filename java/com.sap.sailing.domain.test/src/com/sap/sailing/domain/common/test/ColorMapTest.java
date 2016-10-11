package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.common.ColorMap;
import com.sap.sailing.domain.common.impl.ColorMapImpl;
import com.sap.sse.common.Color;

public class ColorMapTest {
    /**
     * Test that for 100 iterations we don't get two equal colors
     */
    @Test
    public void test100DifferentColors() {
        final Set<Color> distinctColors = new HashSet<>();
        ColorMap<Integer> colorMap = new ColorMapImpl<Integer>();
        for (int i=0; i<100; i++) {
            Color color = colorMap.getColorByID(i);
            assertTrue("Color "+color+" for integer "+i+" was already in the set", distinctColors.add(color));
        }
    }
}
