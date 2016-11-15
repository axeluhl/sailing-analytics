package com.sap.sailing.polars.jaxrs.api.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.SharedDomainFactoryImpl;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;

public class PolarDataResourceTest {

    private static final double[] SPEED_FUNCTIONS_COEFFS_DOWNWIND = 
            new double[]{ 0, 0.1740513714125882, 0.07943929197949728, -0.00455702903757782 };
    private static final double[] ANGLE_FUNCTION_COEFFS_DOWNWIND = 
            new double[]{ 172.54379549250007, -3.369846070650965, 0.6635660994215868, -0.03653517733437184 };
    private static final String BOAT_CLASS = "Laser Radial";

    private PolarDataServiceImpl polarDataService;
    private SharedDomainFactory sharedDomainFactory;

    @Before
    public void setUp() throws IOException, ParseException {
        polarDataService = new PolarDataServiceImpl();
        sharedDomainFactory = new SharedDomainFactoryImpl(/* raceLogResolver */ null);
        
        PolarDataClientMock client = new PolarDataClientMock(new File("resources/polar_data.json"), polarDataService, sharedDomainFactory);
        client.updatePolarDataRegressions();
    }

    /**
     * Test to check if client importing data correctly. Using {@link PolarDataClientMock} which use {@link File}
     * polar_data.json as source
     * 
     * @throws NotEnoughDataHasBeenAddedException
     */
    @Test
    public void testImportingFromMockFile() throws NotEnoughDataHasBeenAddedException {
        BoatClass boatClass = sharedDomainFactory.getOrCreateBoatClass(BOAT_CLASS);
        PolynomialFunction angleDownwindFunction = new PolynomialFunction(ANGLE_FUNCTION_COEFFS_DOWNWIND);
        PolynomialFunction speedDownwindFunction = new PolynomialFunction(SPEED_FUNCTIONS_COEFFS_DOWNWIND);

        assertThat(polarDataService.getSpeedRegressionsPerAngle().size(), is(32));
        assertThat(polarDataService.getCubicRegressionsPerCourse().size(), is(2));
        assertThat(polarDataService.getFixCointPerBoatClass().get(boatClass), is(62117L));
        // presuming that if downwind functions & regression collections' size are correct then any other thing is
        // imported correctly
        assertThat(polarDataService.getAngleRegressionFunction(boatClass, LegType.DOWNWIND), is(angleDownwindFunction));
        assertThat(polarDataService.getSpeedRegressionFunction(boatClass, LegType.DOWNWIND), is(speedDownwindFunction));
    }

}
