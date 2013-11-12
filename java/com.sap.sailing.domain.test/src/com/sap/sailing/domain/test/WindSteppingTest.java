package com.sap.sailing.domain.test;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.domain.common.WindStepping;
import com.sap.sailing.domain.common.impl.WindSteppingImpl;
import com.sap.sailing.domain.common.impl.WindSteppingWithMaxDistance;

public class WindSteppingTest {
    
    @Test
    public void testStepping() {
        Integer[] levels = {2,4};
        WindStepping stepping = new WindSteppingImpl(levels);
        Assert.assertEquals(0, stepping.getLevelIndexForValue(1));
        Assert.assertEquals(1, stepping.getLevelIndexForValue(5));
        Assert.assertEquals(1, stepping.getLevelIndexForValue(3));
        Assert.assertEquals(2, stepping.getSteppedValueForValue(1.064));
        Assert.assertEquals(4, stepping.getSteppedValueForValue(7.8365));
    }
    
    @Test
    public void testSteppingWithMaxDistance() {
        Integer[] levels = {2,4};
        WindStepping stepping = new WindSteppingWithMaxDistance(levels, 1.0);
        Assert.assertEquals(0, stepping.getLevelIndexForValue(1));
        Assert.assertEquals(1, stepping.getLevelIndexForValue(5));
        Assert.assertEquals(-1, stepping.getLevelIndexForValue(5.01));
        Assert.assertEquals(-1, stepping.getLevelIndexForValue(0.5));
        Assert.assertEquals(-1, stepping.getLevelIndexForValue(8));
        Assert.assertEquals(2, stepping.getSteppedValueForValue(1.064));
        Assert.assertEquals(-1, stepping.getSteppedValueForValue(7.8365));
    }

}
