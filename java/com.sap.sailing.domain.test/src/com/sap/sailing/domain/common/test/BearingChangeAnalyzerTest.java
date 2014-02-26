package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertFalse;
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

    @Test
    public void testNegatigeSimpleNonCrossingZeroDegreesCase() {
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), 10, new DegreeBearingImpl(15),
                new DegreeBearingImpl(17)));
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), 10, new DegreeBearingImpl(15),
                new DegreeBearingImpl(3)));
    }

    @Test
    public void testForwardCrossingZeroDegreesCase() {
        assertTrue(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), 10, new DegreeBearingImpl(5),
                new DegreeBearingImpl(0)));
        assertTrue(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), 10, new DegreeBearingImpl(5),
                new DegreeBearingImpl(359)));
        assertTrue(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), 10, new DegreeBearingImpl(5),
                new DegreeBearingImpl(1)));
    }

    @Test
    public void testNegatigeForwardCrossingZeroDegreesCase() {
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), 10, new DegreeBearingImpl(5),
                new DegreeBearingImpl(354)));
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), 10, new DegreeBearingImpl(5),
                new DegreeBearingImpl(6)));
    }

    @Test
    public void testBackwardNonCrossingZeroDegreesCase() {
        assertTrue(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), -350, new DegreeBearingImpl(5),
                new DegreeBearingImpl(180)));
    }

    @Test
    public void testNegatigeBackwardNonCrossingZeroDegreesCase() {
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), -350, new DegreeBearingImpl(5),
                new DegreeBearingImpl(359)));
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), -350, new DegreeBearingImpl(5),
                new DegreeBearingImpl(0)));
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(355), -350, new DegreeBearingImpl(5),
                new DegreeBearingImpl(1)));
    }

    @Test
    public void testBackwardCrossingZeroDegreesCase() {
        assertTrue(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), -10, new DegreeBearingImpl(355),
                new DegreeBearingImpl(0)));
        assertTrue(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), -10, new DegreeBearingImpl(355),
                new DegreeBearingImpl(359)));
        assertTrue(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), -10, new DegreeBearingImpl(355),
                new DegreeBearingImpl(1)));
    }

    @Test
    public void testNegatigeBackwardCrossingZeroDegreesCase() {
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), -10, new DegreeBearingImpl(355),
                new DegreeBearingImpl(180)));
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), -10, new DegreeBearingImpl(355),
                new DegreeBearingImpl(354)));
        assertFalse(bearingChangeAnalyzer.didPass(new DegreeBearingImpl(5), -10, new DegreeBearingImpl(355),
                new DegreeBearingImpl(6)));
    }
}
