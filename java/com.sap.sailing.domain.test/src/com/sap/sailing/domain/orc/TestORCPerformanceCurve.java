package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveCourseImpl;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sailing.domain.orc.impl.ORCCertificatesJsonImporter;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * 
 * @author Daniel Lisunkin {i505543)
 *
 */
public class TestORCPerformanceCurve {

    // set true to see all the differences i
    private final boolean collectErrors = true;
    
    private static ORCPerformanceCurveCourse alturaCourse;
    private static ORCCertificatesCollection importerLocal;
    private static ORCCertificatesCollection importerOnline;
    
    private static final String RESOURCES = "resources/orc/";
    @Rule
    public IgnoreInvalidOrcCerticatesRule customIgnoreRule = new IgnoreInvalidOrcCerticatesRule();

    @Rule
    public ErrorCollector collector = new ErrorCollector();
    
    public void assertEquals(double a, double b, double accuracy) {
        try {
            Assert.assertEquals(a, b, accuracy);
        } catch (AssertionError e) {
            if (collectErrors) {
                collector.addError(e);
            }
        }
    }
    
    @BeforeClass
    public static void initialize() throws IOException, ParseException {
        List<ORCPerformanceCurveLeg> legs = new ArrayList<>();
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2.23), new DegreeBearingImpl(10)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2.00), new DegreeBearingImpl(170)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(0.97), new DegreeBearingImpl(0)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.03), new DegreeBearingImpl(15)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.03), new DegreeBearingImpl(165)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.17), new DegreeBearingImpl(180)));
        alturaCourse = new ORCPerformanceCurveCourseImpl(legs);         //this course is the same course as seen in the Altura "IMS Explanation" sheet
        
        // Local File:
        File fileGER = new File(RESOURCES + "GER2019.json");
        importerLocal = new ORCCertificatesJsonImporter().read(new FileInputStream(fileGER));
        
        // Online File:
        importerOnline = new ORCCertificatesJsonImporter().read(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
    }
    
    @Test
    public void testLagrangeInterpolation60() throws FunctionEvaluationException {
        ORCCertificate certificate = importerLocal.getCertificateById("GER140772GER5549");
        testAllowancePerLeg(certificate, 60.0, 498.4);
    }
    
    @Test
    public void testLagrangeInterpolation62_5() throws FunctionEvaluationException {
        ORCCertificate certificate = importerLocal.getCertificateById("GER140772GER5549");
        testAllowancePerLeg(certificate, 62.5, 492.2, 425.0, 403.6, 393.1, 386.7, 382.6, 371.4);
    }
    
    @Test
    public void testLagrangeInterpolation98_3() throws FunctionEvaluationException {
        ORCCertificate certificate = importerLocal.getCertificateById("GER140772GER5549");
        testAllowancePerLeg(certificate, 98.3, 483.6, 418.7, 394.8, 377.9, 360.2, 345.0, 321.7);
    }
    
    @Test
    public void testLagrangeInterpolation120() throws FunctionEvaluationException {
        ORCCertificate certificate = importerLocal.getCertificateById("GER140772GER5549");
        testAllowancePerLeg(certificate, 120.0, 506);
    }
    
    @Test
    public void testLagrangeInterpolation138_7() throws FunctionEvaluationException {
        ORCCertificate certificate = importerLocal.getCertificateById("GER140772GER5549");
        testAllowancePerLeg(certificate, 138.7, 588.1, 468.4, 413.8, 382.6, 355.1, 326.4, 275.4);
    }
    
    private void testAllowancePerLeg(ORCCertificate certificate, double twa, double... expectedAllowancesPerTrueWindSpeed) throws FunctionEvaluationException {
        Double accuracy = 0.1;
        final ORCPerformanceCurveCourse course = new ORCPerformanceCurveCourseImpl(Arrays.asList(new ORCPerformanceCurveLegImpl(ORCCertificateImpl.NAUTICAL_MILE, new DegreeBearingImpl(twa))));
        final ORCPerformanceCurve performanceCurve = new ORCPerformanceCurveImpl(certificate, course);
        for (int i=0; i<expectedAllowancesPerTrueWindSpeed.length; i++) {
            assertEquals(expectedAllowancesPerTrueWindSpeed[i], performanceCurve.getAllowancePerCourse(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i]).asSeconds(), accuracy);
        }
    }
    
    @Test
    public void testSimpleConstructedCourse() throws FunctionEvaluationException {
        ORCCertificateImpl certificate = (ORCCertificateImpl) importerLocal.getCertificateById("GER140772GER5549");
        List<ORCPerformanceCurveLeg> legs = new ArrayList<>();
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(0)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(30)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(60)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(120)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(180)));
        ORCPerformanceCurveCourse simpleCourse = new ORCPerformanceCurveCourseImpl(legs);
        ORCPerformanceCurve performanceCurve = new ORCPerformanceCurveImpl(certificate, simpleCourse);
        assertNotNull(performanceCurve);
        testAllowancePerCourse(performanceCurve, 654.4, 538.6, 485.1, 458.7, 444.1, 430.3, 404.3);
    }

    private void testAllowancePerCourse(ORCPerformanceCurve performanceCurve, double... allowancePerNauticalMileInSeconds) throws ArgumentOutsideDomainException {
        for (int i=0; i<allowancePerNauticalMileInSeconds.length; i++) {
            assertEquals(allowancePerNauticalMileInSeconds[i]*performanceCurve.getCourse().getTotalLength().getNauticalMiles(),
                    performanceCurve.getAllowancePerCourse(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i]).asSeconds(), 0.3);
        }
    }

    @Test
    public void testPerformanceCurveInversion() throws MaxIterationsExceededException, FunctionEvaluationException {
        Double accuracy = 0.1;
        ORCCertificateImpl certificate = (ORCCertificateImpl) importerLocal.getCertificateById("GER140772GER5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) new ORCPerformanceCurveImpl(certificate, alturaCourse);
        testBackwardForward(accuracy, performanceCurve, 11.5);
        testBackwardForward(accuracy, performanceCurve, 17.23);
        testBackwardForward(accuracy, performanceCurve, 18);
        testForwardBackward(accuracy, performanceCurve, 450);
        testForwardBackward(accuracy, performanceCurve, 500);
        testForwardBackward(accuracy, performanceCurve, 600);
        testForwardBackward(accuracy, performanceCurve, 700);
        testForwardBackward(accuracy, performanceCurve, 750);
    }

    private void testBackwardForward(Double accuracy, ORCPerformanceCurveImpl performanceCurve, final double impliedWindInKnots)
            throws MaxIterationsExceededException, FunctionEvaluationException, ArgumentOutsideDomainException {
        assertEquals(impliedWindInKnots, performanceCurve.getImpliedWind(performanceCurve.getAllowancePerCourse(new KnotSpeedImpl( impliedWindInKnots))).getKnots(), accuracy);
    }

    private void testForwardBackward(Double accuracy, ORCPerformanceCurveImpl performanceCurve, final double secondsPerNauticalMile)
            throws ArgumentOutsideDomainException, MaxIterationsExceededException, FunctionEvaluationException {
        final double secondsForCourse = secondsPerNauticalMile*alturaCourse.getTotalLength().getNauticalMiles();
        assertEquals(secondsForCourse,
                performanceCurve.getAllowancePerCourse(performanceCurve.getImpliedWind(
                        Duration.ONE_SECOND.times(secondsForCourse))).asSeconds(), accuracy);
    }
    
    // Tests for a Implied Wind calculation for a simple predefined course. The solutions are extracted from the provided ORC TestPCS.exe application.
    @Test
    public void testImpliedWind() throws MaxIterationsExceededException, FunctionEvaluationException {
       double accuracy = 0.0001;
       ORCCertificate certificateMoana          = importerLocal.getCertificateById("GER140772GER5549");
       ORCCertificate certificateMilan          = importerLocal.getCertificateById("GER166844GER7323");
       ORCCertificate certificateTutima         = importerLocal.getCertificateById("GER140618GER5609");
       ORCCertificate certificateBank           = importerLocal.getCertificateById("GER140755GER5555");
       ORCCertificate certificateHaspa          = importerLocal.getCertificateById("GER141411GER6300");
       ORCCertificate certificateHalbtrocken    = importerLocal.getCertificateById("GER141432GER5564");
       ORCPerformanceCurve performanceCurveMoana        = new ORCPerformanceCurveImpl(certificateMoana, alturaCourse);
       ORCPerformanceCurve performanceCurveMilan        = new ORCPerformanceCurveImpl(certificateMilan, alturaCourse);
       ORCPerformanceCurve performanceCurveTutima       = new ORCPerformanceCurveImpl(certificateTutima, alturaCourse);
       ORCPerformanceCurve performanceCurveBank         = new ORCPerformanceCurveImpl(certificateBank, alturaCourse);
       ORCPerformanceCurve performanceCurveHaspa        = new ORCPerformanceCurveImpl(certificateHaspa, alturaCourse);
       ORCPerformanceCurve performanceCurveHalbtrocken  = new ORCPerformanceCurveImpl(certificateHalbtrocken, alturaCourse);
       // Test for corner case and if the algorithm reacts to the boundaries of 6 and 20 kts.
       assertEquals( 6.0    , performanceCurveMoana.getImpliedWind(Duration.ONE_HOUR.times(24)).getKnots(), accuracy);
       assertEquals(20.0    , performanceCurveMoana.getImpliedWind(Duration.ONE_HOUR.divide(24)).getKnots(), accuracy);
       assertEquals(1.0, performanceCurveMilan.getAllowancePerCourse(new KnotSpeedImpl(12.80881)).asHours(), accuracy); 
       // scratch sheets and implied wind as calculated by Altura for course1 and 1:00:00 / 1:30:00 time sailed, respectively:
       //               6kts    8kts    10kts   12kts   14kts   16kts   20kts   implied wind    Altura          ORC Scorer      ORC PCS Test    SAP
       // Milan:        675.2   539.5   473.1   437.6   412.7   388.8   350.8                   12.8091135      12.80881        12.80881        12.809089
       // Moana:        775.7   627.5   549.9   512.4   493.3   473.1   435.0                   7.76029797      7.76218         7.76218         7.7602936
       
       // scratch sheet as computed by ORC Scorer:
       //               6kts    8kts    10kts   12kts   14kts   16kts   20kts
       // Milan:        675.2   539.5   473.1   437.6   412.7   388.8   350.8
       // Moana:        775.7   627.5   549.9   512.4   493.3   473.1   435.0
       assertEquals(12.80881 , performanceCurveMilan.getImpliedWind(Duration.ONE_HOUR.times(1.0)).getKnots(), accuracy);
       assertEquals(8.72668  , performanceCurveTutima     .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(8.07591  , performanceCurveBank       .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.78413  , performanceCurveHaspa      .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.76218  , performanceCurveMoana      .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.62407  , performanceCurveHalbtrocken.getImpliedWind(Duration.ONE_HOUR.times(2.0)).getKnots(), accuracy);
    }
    
    // Tests for a Allowance calculation for a simple predefined course and given Implied Winds. The solutions are extracted from the provided ORC TestPCS.exe
    @Test
    public void testAllowances() throws FunctionEvaluationException {
        double accuracy = 0.00001;
        ORCCertificate certificateMoana          = importerLocal.getCertificateById("GER140772GER5549");
        ORCCertificate certificateMilan          = importerLocal.getCertificateById("GER166844GER7323");
        ORCCertificate certificateTutima         = importerLocal.getCertificateById("GER140618GER5609");
        ORCCertificate certificateBank           = importerLocal.getCertificateById("GER140755GER5555");
        ORCCertificate certificateHaspa          = importerLocal.getCertificateById("GER141411GER6300");
        ORCCertificate certificateHalbtrocken    = importerLocal.getCertificateById("GER141432GER5564");
        ORCPerformanceCurve performanceCurveMoana        = new ORCPerformanceCurveImpl(certificateMoana, alturaCourse);
        ORCPerformanceCurve performanceCurveMilan        = new ORCPerformanceCurveImpl(certificateMilan, alturaCourse);
        ORCPerformanceCurve performanceCurveTutima       = new ORCPerformanceCurveImpl(certificateTutima, alturaCourse);
        ORCPerformanceCurve performanceCurveBank         = new ORCPerformanceCurveImpl(certificateBank, alturaCourse);
        ORCPerformanceCurve performanceCurveHaspa        = new ORCPerformanceCurveImpl(certificateHaspa, alturaCourse);
        ORCPerformanceCurve performanceCurveHalbtrocken  = new ORCPerformanceCurveImpl(certificateHalbtrocken, alturaCourse);
        assertEquals(Duration.ONE_HOUR.times(1.0).asHours(), performanceCurveMilan.getAllowancePerCourse(new KnotSpeedImpl(12.80881)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(1.5).asHours(), performanceCurveTutima.getAllowancePerCourse(new KnotSpeedImpl(8.72668)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(1.5).asHours(), performanceCurveBank.getAllowancePerCourse(new KnotSpeedImpl(8.07591)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(1.5).asHours(), performanceCurveHaspa.getAllowancePerCourse(new KnotSpeedImpl(7.78413)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(1.5).asHours(), performanceCurveMoana.getAllowancePerCourse(new KnotSpeedImpl(7.76218)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(2.0).asHours(), performanceCurveHalbtrocken.getAllowancePerCourse(new KnotSpeedImpl(7.62407)).asHours(), accuracy);
   }
    
    /**
     * Tests to make sure, that the structure of the certificate files didn't change and performance curves can be built
     */
//    @Ignore("Certificate used for testing no longer valid after 2019")
    @IgnoreInvalidOrcCerticates
    @Test
    public void testOnlineImport() throws FunctionEvaluationException {
        assertFalse(Util.isEmpty(importerOnline.getCertificateIds()));
        for (final ORCCertificate certificate : importerOnline.getCertificates()) {
            new ORCPerformanceCurveImpl(certificate, alturaCourse);
        }
    }
    
    // Tests for the calculations with a more complex course which contains some special leg types. (circular random or other)
    @Test
    public void testComplexCourseImpliedWind() throws FunctionEvaluationException, MaxIterationsExceededException {
        List<ORCPerformanceCurveLeg> list = new ArrayList<>();
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.5), new DegreeBearingImpl(0)));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2), ORCPerformanceCurveLegTypes.WINDWARD_LEEWARD));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2), ORCPerformanceCurveLegTypes.LONG_DISTANCE));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2), ORCPerformanceCurveLegTypes.CIRCULAR_RANDOM));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.5), ORCPerformanceCurveLegTypes.NON_SPINNAKER));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1), new DegreeBearingImpl(180)));
        ORCPerformanceCurveCourse complexCourse = new ORCPerformanceCurveCourseImpl(list);
        
        double accuracy = 0.0001;
        ORCCertificate certificateMoana          = importerLocal.getCertificateById("GER140772GER5549");
        ORCCertificate certificateMilan          = importerLocal.getCertificateById("GER166844GER7323");
        ORCCertificate certificateTutima         = importerLocal.getCertificateById("GER140618GER5609");
        ORCCertificate certificateBank           = importerLocal.getCertificateById("GER140755GER5555");
        ORCCertificate certificateHaspa          = importerLocal.getCertificateById("GER141411GER6300");
        ORCCertificate certificateHalbtrocken    = importerLocal.getCertificateById("GER141432GER5564");
        ORCPerformanceCurve performanceCurveMoana        = new ORCPerformanceCurveImpl(certificateMoana, complexCourse);
        ORCPerformanceCurve performanceCurveMilan        = new ORCPerformanceCurveImpl(certificateMilan, complexCourse);
        ORCPerformanceCurve performanceCurveTutima       = new ORCPerformanceCurveImpl(certificateTutima, complexCourse);
        ORCPerformanceCurve performanceCurveBank         = new ORCPerformanceCurveImpl(certificateBank, complexCourse);
        ORCPerformanceCurve performanceCurveHaspa        = new ORCPerformanceCurveImpl(certificateHaspa, complexCourse);
        ORCPerformanceCurve performanceCurveHalbtrocken  = new ORCPerformanceCurveImpl(certificateHalbtrocken, complexCourse);
        assertEquals(15.75777 , performanceCurveMilan      .getImpliedWind(Duration.ONE_HOUR.times(1.0)).getKnots(), accuracy);
        assertEquals(15.27808 , performanceCurveBank       .getImpliedWind(Duration.ONE_HOUR.times(1.25)).getKnots(), accuracy);
        assertEquals(15.10141 , performanceCurveMoana      .getImpliedWind(Duration.ONE_HOUR.times(1.25)).getKnots(), accuracy);
        assertEquals(14.44527 , performanceCurveHaspa      .getImpliedWind(Duration.ONE_HOUR.times(1.25)).getKnots(), accuracy);
        assertEquals(10.86927 , performanceCurveTutima     .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
        assertEquals(9.13385  , performanceCurveHalbtrocken.getImpliedWind(Duration.ONE_HOUR.times(2.0)).getKnots(), accuracy);
    }
    
    @Test
    public void testComplexCourseAllowances() throws FunctionEvaluationException {
        List<ORCPerformanceCurveLeg> list = new ArrayList<>();
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.5), new DegreeBearingImpl(0)));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2), ORCPerformanceCurveLegTypes.WINDWARD_LEEWARD));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2), ORCPerformanceCurveLegTypes.LONG_DISTANCE));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2), ORCPerformanceCurveLegTypes.CIRCULAR_RANDOM));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.5), ORCPerformanceCurveLegTypes.NON_SPINNAKER));
        list.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1), new DegreeBearingImpl(180)));
        ORCPerformanceCurveCourse complexCourse = new ORCPerformanceCurveCourseImpl(list);
        
        double accuracy = 0.00001;
        ORCCertificate certificateMoana          = importerLocal.getCertificateById("GER140772GER5549");
        ORCCertificate certificateMilan          = importerLocal.getCertificateById("GER166844GER7323");
        ORCCertificate certificateTutima         = importerLocal.getCertificateById("GER140618GER5609");
        ORCCertificate certificateBank           = importerLocal.getCertificateById("GER140755GER5555");
        ORCCertificate certificateHaspa          = importerLocal.getCertificateById("GER141411GER6300");
        ORCCertificate certificateHalbtrocken    = importerLocal.getCertificateById("GER141432GER5564");
        ORCPerformanceCurve performanceCurveMoana        = new ORCPerformanceCurveImpl(certificateMoana, complexCourse);
        ORCPerformanceCurve performanceCurveMilan        = new ORCPerformanceCurveImpl(certificateMilan, complexCourse);
        ORCPerformanceCurve performanceCurveTutima       = new ORCPerformanceCurveImpl(certificateTutima, complexCourse);
        ORCPerformanceCurve performanceCurveBank         = new ORCPerformanceCurveImpl(certificateBank, complexCourse);
        ORCPerformanceCurve performanceCurveHaspa        = new ORCPerformanceCurveImpl(certificateHaspa, complexCourse);
        ORCPerformanceCurve performanceCurveHalbtrocken  = new ORCPerformanceCurveImpl(certificateHalbtrocken, complexCourse);
        
        assertEquals(Duration.ONE_HOUR.times(1.0).asHours(), performanceCurveMilan.getAllowancePerCourse(new KnotSpeedImpl(15.75777)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(1.25).asHours(), performanceCurveBank.getAllowancePerCourse(new KnotSpeedImpl(15.27808)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(1.25).asHours(), performanceCurveMoana.getAllowancePerCourse(new KnotSpeedImpl(15.10141)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(1.25).asHours(), performanceCurveHaspa.getAllowancePerCourse(new KnotSpeedImpl(14.44527)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(1.5).asHours(), performanceCurveTutima.getAllowancePerCourse(new KnotSpeedImpl(10.86927)).asHours(), accuracy);
        assertEquals(Duration.ONE_HOUR.times(2.0).asHours(), performanceCurveHalbtrocken.getAllowancePerCourse(new KnotSpeedImpl(9.13385)).asHours(), accuracy);
        
    }
    
}
