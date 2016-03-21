package com.sap.sailing.domain.markpassingcalculation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.markpassingcalculation.impl.CandidateFinderImpl.AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;

public class CandidateFinderImplWhiteBoxTest {
    private static class CandidateFinderWithPublicGetProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances extends CandidateFinderImpl {
        public CandidateFinderWithPublicGetProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(
                DynamicTrackedRace race) {
            super(race);
        }

        public Double getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(
                final List<AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine> distancesToStartLineOfOtherCompetitors, boolean startIsLine) {
            return super.getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(distancesToStartLineOfOtherCompetitors, startIsLine);
        }
    }
    
    private CandidateFinderWithPublicGetProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances finder;
    
    @Before
    public void setUp() {
        DynamicTrackedRace trackedRace = mock(DynamicTrackedRace.class);
        final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29er");
        RaceDefinition race = mock(RaceDefinition.class);
        when(trackedRace.getRace()).thenReturn(race);
        when(race.getBoatClass()).thenReturn(boatClass);
        when(race.getCompetitors()).thenReturn(Collections.emptySet());
        finder = new CandidateFinderWithPublicGetProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(trackedRace);
    }
    
    @Test
    public void testOneProbabilityForEmptyList() {
        List<AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine> emptyDistancesToStartLineOfOtherCompetitors = new ArrayList<>();
        final double probability = finder.getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(emptyDistancesToStartLineOfOtherCompetitors,
                /* startIsLine */ true);
        assertEquals(1.0, probability, 0.00001);
    }

    @Test
    public void testHighProbabilityForCloseProximityAndNoLine() {
        List<AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine> distancesToStartLineOfOtherCompetitors = new ArrayList<>();
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(3.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(5.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(2.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(8.0, null));
        final double probability = finder.getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(distancesToStartLineOfOtherCompetitors,
                /* startIsLine */ false);
        assertTrue(probability > 0.95);
    }

    @Test
    public void testHighProbabilityForCloseProximityExceptOneAndNoLine() {
        List<AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine> distancesToStartLineOfOtherCompetitors = new ArrayList<>();
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(3.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(5.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(2.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(8.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(1.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(2.5, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(3.2, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(2739.0, null));
        final double probability = finder.getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(distancesToStartLineOfOtherCompetitors,
                /* startIsLine */ false);
        assertTrue("Expected probability to exceed 55% but got "+probability, probability > 0.55);
    }

    @Test
    public void testLowProbabilityForMostFarAwayAndNoLine() {
        List<AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine> distancesToStartLineOfOtherCompetitors = new ArrayList<>();
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(50.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(250.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(120.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(200.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(300.0, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(2.5, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(3.2, null));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(2739.0, null));
        final double probability = finder.getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(distancesToStartLineOfOtherCompetitors,
                /* startIsLine */ false);
        assertTrue("Expected probability to be below 10% but got "+probability, probability <= 0.1);
    }

    @Test
    public void testHighProbabilityForLateStartAndStartIsLine() {
        List<AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine> distancesToStartLineOfOtherCompetitors = new ArrayList<>();
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(100.0, -80.));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(120.0, -82.));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(130.0, -81.));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(90.0, -79.));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(140.0, -75.));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(150, -88.));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(135, -77.));
        distancesToStartLineOfOtherCompetitors.add(createDistancePair(20.0, -10.)); // another late starter just across the line
        final double probability = finder.getProbabilityOfStartBasedOnOtherCompetitorsStartLineDistances(distancesToStartLineOfOtherCompetitors,
                /* startIsLine */ true);
        assertTrue("Expected probability to be above 85% but got "+probability, probability >= 0.85);
    }

    private AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine createDistancePair(double absoluteDistanceToLine,
            Double signedXTEDistanceFromLine) {
        return new AbsoluteGeometricDistanceAndSignedProjectedDistanceToStartLine(
                new MeterDistance(absoluteDistanceToLine), signedXTEDistanceFromLine == null ? null : new MeterDistance(signedXTEDistanceFromLine));
    }
}
