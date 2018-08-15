package com.sap.sailing.gwt.ui.test;
import static org.junit.Assert.*;
import org.junit.Test;
import com.sap.sailing.gwt.ui.simulator.streamlets.ValueRangeFlexibleBoundaries;

public class ValueRangeFlexibleBoundariesTest {
    //Create new valueRange, boundaries should be [-6,-4] and [4,6].
    ValueRangeFlexibleBoundaries valueRange = new ValueRangeFlexibleBoundaries(-5.0, 5.0, 0.10);
    Double minLeft = -6.0;
    Double maxRight = 6.0;
    @Test
    public void testCheckIfValueIsInLeftBoundaryRangeAndUpdateIfNecessary() {
        //update left border to 0.0, that means boundaries now should be [-0.5,0.5] and [4.5,5.5]
        valueRange.checkIfValueIsInLeftBoundaryRangeAndUpdateIfNecessary(0.0);
        Double newMinLeft = -0.5;
        Double newMaxRight = 5.5;
        assertEquals(newMinLeft, (Double) valueRange.getMinLeft());
        assertEquals(newMaxRight, (Double) valueRange.getMaxRight());
    }
    @Test
    public void testCheckIfValueIsInRightBoundaryRangeAndUpdateIfNecessary() {
        //update right border to 20, that means the boundaries now should be [-7.5,-2.5] and [17.5,22.5]
        valueRange.checkIfValueIsInRightBoundaryRangeAndUpdateIfNecessary(20.0);
        Double newMaxRight = 22.5;
        Double newMinLeft = -7.5;
        assertEquals(newMaxRight, (Double) valueRange.getMaxRight());
        assertEquals(newMinLeft, (Double) valueRange.getMinLeft());
    }
    @Test
    public void testGetMinLeft() {
        assertEquals(minLeft, (Double) valueRange.getMinLeft());
    }
    @Test
    public void testGetMaxRight() {
        assertEquals(maxRight, (Double) valueRange.getMaxRight());
    }
}
