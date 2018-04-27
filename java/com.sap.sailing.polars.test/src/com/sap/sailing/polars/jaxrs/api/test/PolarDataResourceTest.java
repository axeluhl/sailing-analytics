package com.sap.sailing.polars.jaxrs.api.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;

public class PolarDataResourceTest {
    private static final Logger logger = Logger.getLogger(PolarDataResourceTest.class.getName());
    private static final double[] SPEED_FUNCTIONS_COEFFS_DOWNWIND = new double[] { 0, 0.8865470561979691,
            0.018166834270004983, -0.0013589457745686317 };

    private static final double[] ANGLE_FUNCTION_COEFFS_DOWNWIND = new double[] { 83.35341925825924, 7.469179893378168,
            -0.3123881380597595, 0.0046038938594392675 };
    private static final String BOAT_CLASS = "505";

    private PolarDataServiceImpl polarService;
    private DomainFactory domainFactory;

    @Before
    public void setUp() throws IOException, ParseException, ClassNotFoundException, InterruptedException {
        polarService = new PolarDataServiceImpl();
        domainFactory = new DomainFactoryImpl(/* raceLogResolver */ null);
        final PolarDataClientMock client = new PolarDataClientMock(new File("resources/polar_data"), polarService,
                domainFactory);
        client.updatePolarDataRegressions();
        // ensure that setting the domain factory has worked
        polarService.runWithDomainFactory(domainFactory -> {
            try {
                client.updatePolarDataRegressions();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception while trying to import polar data from file during test", e);
            }
        });
    }

    /**
     * Test to check if client importing data correctly. Using {@link PolarDataClientMock} which use {@link File}
     * polar_data as source.<br>
     * The following steps are necessary in order to produce a new polar_data file. In this example the event <i>505
     * Worlds 2015</i> is chosen<br>
     * <table>
     * <ol>
     * <li>Run the local Sailing Server and Sailing GWT from the run configurations</li>
     * <li>Access the local <a href="http://127.0.0.1:8888/gwt/AdminConsole.html"/>Admin Console</a></li>
     * <li>Go to <i>Connector</i>'s tap, select <i>TracTrac Events</i>
     * <li>Select <i>505 Worlds 2015</i> from the selection options and click at <i>List Races</i></li>
     * <li>Mark all of the resulting races, click at <i>Start tracking</i> and wait until they reach <i>FINISHED</i>
     * status</li>
     * <li>Access the local <a href="http://127.0.0.1:8888/polars/api/polar_data">polar data file</a> and store it
     * within this project's /resources folder</i>
     * </ol>
     * </table>
     * 
     * @throws NotEnoughDataHasBeenAdwas
     *             edException
     */
    @Test
    public void testImportingFromMockFile() throws NotEnoughDataHasBeenAddedException {
        BoatClass boatClass = domainFactory.getOrCreateBoatClass(BOAT_CLASS);
        PolynomialFunction angleDownwindFunction = new PolynomialFunction(ANGLE_FUNCTION_COEFFS_DOWNWIND);
        PolynomialFunction speedDownwindFunction = new PolynomialFunction(SPEED_FUNCTIONS_COEFFS_DOWNWIND);

        assertThat(polarService.getSpeedRegressionsPerAngle().size(), is(36));
        assertThat(polarService.getCubicRegressionsPerCourse().size(), is(2));
        assertThat(polarService.getFixCointPerBoatClass().get(boatClass), is(105594L));
        // presuming that if downwind functions & regression collections' size are correct then any other thing is
        // imported correctly
        assertThat(polarService.getAngleRegressionFunction(boatClass, LegType.DOWNWIND), is(angleDownwindFunction));
        assertThat(polarService.getSpeedRegressionFunction(boatClass, LegType.DOWNWIND), is(speedDownwindFunction));
        // assertThat(polarService.get)
    }

}
