package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.common.BearingChangeAnalyzer;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

public class BearingChangeAnalyzerTest {
    private final BearingChangeAnalyzer bearingChangeAnalyzer = BearingChangeAnalyzer.INSTANCE;
    
    @Test
    public void testSimpleNonCrossingZeroDegreesCase() {
        assertTrue(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), 10, new DegreeBearingImpl(15),
                new DegreeBearingImpl(10)));
    }
}
