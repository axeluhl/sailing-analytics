package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import com.sap.sailing.domain.orc.impl.ORCCertificateImpl;
import com.sap.sailing.domain.orc.impl.ORCCertificateImporterJSON;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveCourseImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class TestORCPerformanceCurve {

    // for the usage of 
    private final boolean collectErrors = true;
    
    private static final String RESOURCES = "resources/orc/";
    private static ORCPerformanceCurveCourse course1;
    private static ORCCertificateImporter importer;

    @Rule
    public ErrorCollector collector = new ErrorCollector();
    
    public void assertEquals(double a, double b, double accuracy){
        try{
            Assert.assertEquals(a, b, accuracy);
        } catch(AssertionError e){
            if(collectErrors) {
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
        course1 = new ORCPerformanceCurveCourseImpl(legs);
        
        // Local File:
        File fileGER = new File(RESOURCES + "GER2019.json");
        importer = new ORCCertificateImporterJSON(new FileInputStream(fileGER));
        
        // Online File:
        //importer = new ORCCertificateImporterJSON(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
    }
    
    @Test
    public void testLagrangeInterpolation60() throws FunctionEvaluationException {
        ORCCertificate certificate = importer.getCertificate("GER 5549");
        test(certificate, 60.0, 498.4);
    }
    
    @Test
    public void testLagrangeInterpolation62_5() throws FunctionEvaluationException {
        ORCCertificate certificate = importer.getCertificate("GER 5549");
        test(certificate, 62.5, 492.2, 425.0, 403.6, 393.1, 386.7, 382.6, 371.4);
    }
    
    @Test
    public void testLagrangeInterpolation98_3() throws FunctionEvaluationException {
        ORCCertificate certificate = importer.getCertificate("GER 5549");
        test(certificate, 98.3, 483.6, 418.7, 394.8, 377.9, 360.2, 345.0, 321.7);
    }
    
    @Test
    public void testLagrangeInterpolation120() throws FunctionEvaluationException {
        ORCCertificate certificate = importer.getCertificate("GER 5549");
        test(certificate, 120.0, 506);
    }
    
    @Test
    public void testLagrangeInterpolation138_7() throws FunctionEvaluationException {
        ORCCertificate certificate = importer.getCertificate("GER 5549");
        test(certificate, 138.7, 588.1, 468.4, 413.8, 382.6, 355.1, 326.4, 275.4);
    }
    
    private void test(ORCCertificate certificate, double twa, double... expectedAllowancesPerTrueWindSpeed) throws FunctionEvaluationException {
        Double accuracy = 0.1;
        final ORCPerformanceCurveCourse course = new ORCPerformanceCurveCourseImpl(Arrays.asList(new ORCPerformanceCurveLegImpl(ORCCertificateImpl.NAUTICAL_MILE, new DegreeBearingImpl(twa))));
        final ORCPerformanceCurve performanceCurve = certificate.getPerformanceCurve(course);
        for (int i=0; i<expectedAllowancesPerTrueWindSpeed.length; i++) {
            assertEquals(expectedAllowancesPerTrueWindSpeed[i], performanceCurve.getAllowancePerCourse(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i]).asSeconds(), accuracy);
        }
    }
    
    @Test
    public void testSimpleConstructedCourse() throws FunctionEvaluationException {
        ORCCertificateImpl certificate = (ORCCertificateImpl) importer.getCertificate("GER 5549");
        List<ORCPerformanceCurveLeg> legs = new ArrayList<>();
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(0)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(30)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(60)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(120)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(180)));
        ORCPerformanceCurveCourse simpleCourse = new ORCPerformanceCurveCourseImpl(legs);
        ORCPerformanceCurve performanceCurve = certificate.getPerformanceCurve(simpleCourse);
        
        assertNotNull(performanceCurve);
        testAllowance(performanceCurve, 654.4, 538.6, 485.1, 458.7, 444.1, 430.3, 404.3);
    }

    private void testAllowance(ORCPerformanceCurve performanceCurve, double... allowancePerNauticalMileInSeconds) throws ArgumentOutsideDomainException {
        for (int i=0; i<allowancePerNauticalMileInSeconds.length; i++) {
            assertEquals(allowancePerNauticalMileInSeconds[i]*performanceCurve.getCourse().getTotalLength().getNauticalMiles(),
                    performanceCurve.getAllowancePerCourse(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[i]).asSeconds(), 0.1);
        }
    }

    @Test
    public void testPerformanceCurveInvertation() throws MaxIterationsExceededException, FunctionEvaluationException {
        Double accuracy = 0.1;
        ORCCertificateImpl certificate = (ORCCertificateImpl) importer.getCertificate("GER 5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve(course1);
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

    private void testForwardBackward(Double accuracy, ORCPerformanceCurveImpl performanceCurve, final int secondsPerNauticalMile)
            throws ArgumentOutsideDomainException, MaxIterationsExceededException, FunctionEvaluationException {
        assertEquals(secondsPerNauticalMile*course1.getTotalLength().getNauticalMiles(),
                performanceCurve.getAllowancePerCourse(performanceCurve.getImpliedWind(Duration.ONE_SECOND.times(secondsPerNauticalMile))).asSeconds(), accuracy);
    }
    
    // Tests for a Implied Wind calculation for a simple predefined course. The solutions are extracted from the provided ORC TestPCS.exe application. 
    @Test
    public void testImpliedWindSimple() throws MaxIterationsExceededException, FunctionEvaluationException {
       double accuracy = 0.000001;
       ORCCertificate certificateMoana          = importer.getCertificate("GER 5549");
       ORCCertificate certificateMilan          = importer.getCertificate("GER 7323");
       ORCCertificate certificateTutima         = importer.getCertificate("GER 5609");
       ORCCertificate certificateBank           = importer.getCertificate("GER 5555");
       ORCCertificate certificateHaspa          = importer.getCertificate("GER 6300");
       ORCCertificate certificateHalbtrocken    = importer.getCertificate("GER 5564");
       ORCPerformanceCurve performanceCurveMoana        = certificateMoana.getPerformanceCurve(course1);
       ORCPerformanceCurve performanceCurveMilan        = certificateMilan.getPerformanceCurve(course1);
       ORCPerformanceCurve performanceCurveTutima       = certificateTutima.getPerformanceCurve(course1);
       ORCPerformanceCurve performanceCurveBank         = certificateBank.getPerformanceCurve(course1);
       ORCPerformanceCurve performanceCurveHaspa        = certificateHaspa.getPerformanceCurve(course1);
       ORCPerformanceCurve performanceCurveHalbtrocken  = certificateHalbtrocken.getPerformanceCurve(course1);
       assertNotNull(performanceCurveMoana);
       assertNotNull(performanceCurveMilan);
       // Test for corner case and if the algorithm reacts to the boundaries of 6 and 20 kts.
       assertEquals( 6.0    , performanceCurveMoana.getImpliedWind(Duration.ONE_HOUR.times(24)).getKnots(), accuracy);
       assertEquals(20.0    , performanceCurveMoana.getImpliedWind(Duration.ONE_HOUR.divide(24)).getKnots(), accuracy);
       assertEquals(12.89281, performanceCurveMilan      .getImpliedWind(Duration.ONE_HOUR.times(1.0)).getKnots(), accuracy);
       assertEquals(8.72668 , performanceCurveTutima     .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(8.07591 , performanceCurveBank       .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.78413 , performanceCurveHaspa      .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.76218 , performanceCurveMoana      .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.62407 , performanceCurveHalbtrocken.getImpliedWind(Duration.ONE_HOUR.times(2.0)).getKnots(), accuracy);
    }
    
}
