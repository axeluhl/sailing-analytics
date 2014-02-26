package com.sap.sailing.polars.test;

import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.analysis.impl.PolarSheetAnalyzerImpl;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;

public class PolarSheetAnalyzerTest {

    @Test
    public void testOptimalUpwindSpeedCalculation() {
        PolarSheetGenerationSettings settings = PolarSheetGenerationSettingsImpl.createStandardPolarSettings();
        Number[][] averagedPolarDataByWindSpeed = {
                {},
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0.2, 0.3, 0.4, 0.5, 0.501, 0.502, 0.503, 0.504, 0.505, 0.506, 0.507,
                        0.508, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, {} };
        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarDataByWindSpeed, 0, null,
                settings.getWindStepping(), null);
        PolarSheetAnalyzer analyzer = new PolarSheetAnalyzerImpl(new MockedPolarDataService(null, data));
        SpeedWithBearing result = analyzer.getOptimalUpwindSpeedWithBearingFor(null, new KnotSpeedImpl(6));
        Assert.assertEquals(0.5 * Math.cos(Math.toRadians(45)), result.getKnots(), 0.01);
        Assert.assertEquals(45, result.getBearing().getDegrees(), 0.01);
    }

    @Test
    public void testOptimalDownwindSpeedCalculation() {
        PolarSheetGenerationSettings settings = PolarSheetGenerationSettingsImpl.createStandardPolarSettings();
        Number[][] averagedPolarDataByWindSpeed = {
                {},
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4.5, 4, 3, 2, 1, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0 }, {} };
        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarDataByWindSpeed, 0, null,
                settings.getWindStepping(), null);
        PolarSheetAnalyzer analyzer = new PolarSheetAnalyzerImpl(new MockedPolarDataService(null, data));
        SpeedWithBearing result = analyzer.getOptimalDownwindSpeedWithBearingFor(null, new KnotSpeedImpl(6));
        Assert.assertEquals(4.5 * Math.sin(Math.toRadians(85)), result.getKnots(), 0.01);
        Assert.assertEquals(175, result.getBearing().getDegrees(), 0.01);
    }

    private class MockedPolarDataService extends PolarDataServiceImpl {

        private PolarSheetsData mockedData;

        public MockedPolarDataService(Executor executor, PolarSheetsData mockedData) {
            super(executor);
            this.mockedData = mockedData;
        }

        @Override
        public PolarSheetsData getPolarSheetForBoatClass(BoatClass boatClass) {
            return mockedData;
        }

    }

}
