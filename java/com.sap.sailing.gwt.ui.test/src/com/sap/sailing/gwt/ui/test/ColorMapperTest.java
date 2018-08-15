package com.sap.sailing.gwt.ui.test;
import static org.junit.Assert.*;
import org.junit.Test;
import com.sap.sailing.gwt.ui.simulator.streamlets.ColorMapper;
import com.sap.sailing.gwt.ui.simulator.streamlets.ValueRangeFlexibleBoundaries;

public class ColorMapperTest {
    ValueRangeFlexibleBoundaries valueRange = new ValueRangeFlexibleBoundaries(-5.0, 5.0, 0.0);
    ColorMapper colorMapperTestColored = new ColorMapper(valueRange, false);
    ColorMapper colorMapperTestGrey = new ColorMapper(valueRange, true);
    double testValue1 = -5.0;
    String expectedResult1colored = "hsl(240, 100%, 50%)";
    String expectedResult1grey = "rgba(255,255,255,0.0)";
    double testValue2 = 5.0;
    String expectedResult2colored = "hsl(0, 100%, 50%)";
    String expectedResult2grey = "rgba(255,255,255,1.0)";
    @Test
    public void testGetColor() {
        assertEquals(expectedResult1colored, colorMapperTestColored.getColor(testValue1));
        assertEquals(expectedResult1grey, colorMapperTestGrey.getColor(testValue1));
        assertEquals(expectedResult2colored, colorMapperTestColored.getColor(testValue2));
        assertEquals(expectedResult2grey, colorMapperTestGrey.getColor(testValue2));
    }
}
