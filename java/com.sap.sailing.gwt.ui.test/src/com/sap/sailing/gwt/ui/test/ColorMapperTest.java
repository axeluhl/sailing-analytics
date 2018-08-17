package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.gwt.ui.simulator.streamlets.ColorMapper;
import com.sap.sailing.gwt.ui.simulator.streamlets.ColorMapperChangedListener;
import com.sap.sailing.gwt.ui.simulator.streamlets.ValueRangeFlexibleBoundaries;

public class ColorMapperTest {
    ColorMapper colorMapper;
    ColorMapperChangedListener listener;
    ValueRangeFlexibleBoundaries valueRange;
    Set<Integer> valueSet;
    Set<String> colorSet;

    @Before
    public void setUp() {
        valueRange = new ValueRangeFlexibleBoundaries(-5.0, 5.0, 0.0);
        colorMapper = new ColorMapper(valueRange, false);
        listener = mock(ColorMapperChangedListener.class);
        colorMapper.addListener(listener);
        valueSet = new HashSet<>();
        updateValueSet(valueSet);
        colorSet = new HashSet<>();
        fillColorSet();
    }

    public void updateValueSet(Set<Integer> valueSet) {
        valueSet.clear();
        for (int i = (int) Math.round(valueRange.getMinLeft()); i <= (int) Math.round(valueRange.getMaxRight()); i++) {
            valueSet.add(i);
        }
    }

    public void fillColorSet() {
        for (int i = 0; i <= 240; i++) {
            colorSet.add("hsl(" + i + ", 100%, 50%)");
        }
    }
    
    @Test
    public void testExceptionForValueOutOfBoundaries() {
        try {
            colorMapper.getColor(-10.0);
            fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetColorColored() {
        // first set the color and verify listener will be notified
        colorMapper.setGrey(false);
        verify(listener, times(1)).onColorMappingChanged();
        // Check for each value in valueSet that getColor() returns a color within the colorSet.
        for (int i : valueSet) {
            assertTrue(colorSet.contains(colorMapper.getColor(i)));
        }
    }

    @Test
    public void changeValueRangeAndTestGetColorColored() {
        valueRange.setMinMax(12.0, 26.0);
        // verify Listener
        verify(listener, times(1)).onColorMappingChanged();
        updateValueSet(valueSet);
        // check if new values are within the colorSet
        for (int i : valueSet) {
            assertTrue(colorSet.contains(colorMapper.getColor(i)));
        }
    }
}
