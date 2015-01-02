package com.sap.sailing.polars.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.analysis.impl.PolarSheetAnalyzerImpl;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

public class PolarSheetAnalyzerTest {

    @Test
    public void testSpeedAndBearingCalculation() throws NotEnoughDataHasBeenAddedException {

        PolarSheetAnalyzer analyzer = new PolarSheetAnalyzerImpl(createMockedPolarDataService());
        BoatClass boatClass = mock(BoatClass.class);
        
        SpeedWithBearingWithConfidence<Void> result = analyzer.getAverageUpwindSpeedWithBearingOnStarboardTackFor(boatClass,
                new KnotSpeedImpl(14), true);
        assertThat(result.getObject().getKnots(), closeTo(8.5, 0.1));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(50, 0.1));
        assertThat(result.getConfidence(), closeTo(0.7, 0.1));
        
        SpeedWithBearingWithConfidence<Void> result2 = analyzer.getAverageDownwindSpeedWithBearingOnStarboardTackFor(boatClass,
                new KnotSpeedImpl(14), true);
        assertThat(result2.getObject().getKnots(), closeTo(14.2, 0.1));
        assertThat(result2.getObject().getBearing().getDegrees(), closeTo(142.8, 0.1));
        assertThat(result2.getConfidence(), closeTo(0.6, 0.1));
        
        SpeedWithBearingWithConfidence<Void> result3 = analyzer.getAverageUpwindSpeedWithBearingOnPortTackFor(boatClass,
                new KnotSpeedImpl(14), true);
        assertThat(result3.getObject().getKnots(), closeTo(8.6, 0.1));
        assertThat(result3.getObject().getBearing().getDegrees(), closeTo(-49.5, 0.1));
        assertThat(result3.getConfidence(), closeTo(0.7, 0.1));
        
        SpeedWithBearingWithConfidence<Void> result4 = analyzer.getAverageDownwindSpeedWithBearingOnPortTackFor(boatClass,
                new KnotSpeedImpl(14), true);
        assertThat(result4.getObject().getKnots(), closeTo(13.5, 0.1));
        assertThat(result4.getObject().getBearing().getDegrees(), closeTo(-143.6, 0.1));
        assertThat(result4.getConfidence(), closeTo(0.25, 0.1));
        
    }

    private PolarDataService createMockedPolarDataService() throws NotEnoughDataHasBeenAddedException {
        PolarDataService mockedPolarDataService = mock(PolarDataService.class);
        int[] data = createCounts();
        when(mockedPolarDataService.getDataCountsForWindSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                        any(Integer.class), any(Integer.class))).thenReturn(data);

        //Starboard Upwind
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                        argThat(new BearingMatcher(49)), true)).thenReturn(
                new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.478048671702888), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(50)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.466303997538812), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(51)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.583383077026435), 0.5, null));
        
        //Starboard Downwind
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(141)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.794354053931528), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(142)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.665782579628802), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(143)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(14.169301888320263), 0.5, null));
        
        //Port Upwind
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-48)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.445599410456111), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-50)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.553274292235153), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-49)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.614582090896583), 0.5, null));
        
        //Port Downwind
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-145)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.78894715705271), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-144)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.420656294986587), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-143)), true)).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.268607457651942), 0.5, null));
        
        return mockedPolarDataService;
    }

    /**
     * The following data is taken from a real race. (49ER yellow R2, KW 2014)
     * Only the data for windspeed 14kn is used. The first block contains the 
     * boatspeeds for every angle to the wind. The second block contains the
     * datacount for each angle (number of underlying fixes).
     * 
     * @return
     */
    private int[] createCounts() {
        int[] dataCountsPerAngle = { 0, 0, 2, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 1, 2, 1, 6, 2, 2, 2, 1, 6, 6, 17,
                13, 19, 30, 43, 32, 50, 48, 57, 70, 76, 107, 129, 177, 186, 222, 237, 275, 285, 321, 322, 401, 451,
                466, 413, 468, 479, 459, 429, 360, 345, 317, 288, 278, 202, 175, 130, 113, 104, 100, 73, 45, 51, 52,
                37, 40, 36, 26, 23, 32, 33, 29, 29, 22, 24, 27, 23, 22, 23, 22, 21, 15, 18, 20, 14, 15, 21, 20, 19, 6,
                12, 10, 9, 10, 8, 1, 9, 6, 6, 14, 16, 7, 7, 6, 2, 5, 3, 6, 5, 8, 7, 14, 14, 12, 9, 20, 14, 22, 28, 14,
                20, 22, 29, 30, 38, 45, 50, 72, 87, 117, 114, 110, 145, 148, 156, 165, 150, 194, 224, 211, 196, 199,
                198, 182, 155, 125, 85, 69, 65, 49, 46, 61, 47, 48, 51, 45, 21, 14, 8, 9, 8, 8, 3, 7, 5, 2, 10, 8, 7,
                7, 10, 8, 13, 13, 11, 5, 9, 9, 8, 15, 6, 3, 5, 11, 9, 12, 5, 13, 12, 18, 15, 31, 25, 35, 36, 39, 59,
                71, 55, 52, 64, 72, 66, 70, 89, 104, 81, 120, 130, 117, 107, 113, 137, 136, 126, 108, 103, 92, 85, 76,
                65, 75, 75, 70, 57, 49, 49, 35, 44, 35, 18, 22, 17, 18, 10, 6, 4, 10, 9, 10, 8, 4, 4, 4, 10, 4, 4, 4,
                9, 8, 8, 7, 7, 6, 4, 7, 4, 1, 6, 6, 4, 6, 5, 5, 5, 6, 5, 9, 7, 12, 16, 10, 25, 25, 30, 29, 47, 44, 31,
                69, 68, 79, 62, 66, 86, 72, 84, 89, 75, 73, 62, 104, 110, 147, 137, 170, 204, 310, 347, 354, 377, 433,
                457, 419, 436, 467, 502, 513, 525, 463, 422, 405, 363, 309, 282, 262, 186, 162, 123, 97, 81, 45, 35,
                30, 24, 20, 12, 9, 8, 5, 6, 4, 6, 7, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1 };
        return dataCountsPerAngle;
    }
    
    private class SpeedMatcher extends ArgumentMatcher<Speed> {
        
        private final double speedToMatchInKnots;
        
        public SpeedMatcher(double speedToMatchInKnots) {
            this.speedToMatchInKnots = speedToMatchInKnots;
        }


        @Override
        public boolean matches(Object argument) {
            boolean result = false;
            if (argument != null) {
                Speed speed = (Speed) argument;
                if (speed.getKnots() > speedToMatchInKnots - 0.05 && speed.getKnots() < speedToMatchInKnots + 0.05) {
                    result = true;
                }
            }
            return result;
        }
        
    }
    
    private class BearingMatcher extends ArgumentMatcher<Bearing> {

        private final double bearingToMatchInDegrees;

        public BearingMatcher(double bearingToMatchInDegrees) {
            this.bearingToMatchInDegrees = bearingToMatchInDegrees;
        }

        @Override
        public boolean matches(Object argument) {
            boolean result = false;
            if (argument != null) {
                Bearing bearing = (Bearing) argument;
                if (bearing.getDegrees() > bearingToMatchInDegrees - 0.4999999
                        && bearing.getDegrees() < bearingToMatchInDegrees + 0.49999999) {
                    result = true;
                }
            }
            return result;
        }

    }

}
