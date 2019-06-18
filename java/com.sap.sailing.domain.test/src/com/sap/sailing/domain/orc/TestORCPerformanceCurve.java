package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class TestORCPerformanceCurve {

    private final boolean collectErrors = false;
    
    private static ORCPerformanceCurveCourse course;
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
        course = new ORCPerformanceCurveCourseImpl(legs);
        importer = new ORCCertificateImporterJSON(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
    }
    
    @Test
    public void testLagrangeInterpolation() throws FunctionEvaluationException {
        Double accuracy = 0.21;
        ORCCertificateImpl certificate = (ORCCertificateImpl) importer.getCertificate("GER 5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve(course);
        Double value = performanceCurve.getDurationPerNauticalMileAtTrueWindAngleAndSpeed(new KnotSpeedImpl(6), new DegreeBearingImpl(60)).asSeconds();

        assertNotNull(course);
        assertNotNull(performanceCurve);
        assertEquals(value, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl( 6),new DegreeBearingImpl(  60)).asSeconds(), 0.000001);
        
        assertEquals(492.2, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl( 6),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(425.0, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl( 8),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(403.6, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(10),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(393.1, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(12),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(386.7, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(14),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(382.6, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(16),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(371.4, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(20),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        
        assertEquals(483.6, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl( 6),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(418.7, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl( 8),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(394.8, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(10),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(377.9, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(12),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(360.2, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(14),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(345.0, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(16),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(321.7, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(20),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        
        // TESTS of the corner areas of the polars
        assertEquals(588.1, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl( 6),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(468.4, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl( 8),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(413.8, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(10),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(382.6, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(12),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(355.1, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(14),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(326.4, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(16),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(275.4, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(20),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
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
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve(simpleCourse);
        Map<Speed, Duration> allowancesPerCourse = performanceCurve.createAllowancesPerCourse();
        
        assertNotNull(performanceCurve);
        assertEquals(498.4, performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(6),new DegreeBearingImpl( 60)).asSeconds(), 0.0000000000001);
        assertEquals(506  , performanceCurve.getLagrangeAllowancePerTrueWindSpeedAndAngle(new KnotSpeedImpl(6),new DegreeBearingImpl(120)).asSeconds(), 0.0000000000001);
        assertEquals(654.4, allowancesPerCourse.get(new KnotSpeedImpl( 6)).asSeconds(), 0.1);
        assertEquals(538.6, allowancesPerCourse.get(new KnotSpeedImpl( 8)).asSeconds(), 0.1);
        assertEquals(485.1, allowancesPerCourse.get(new KnotSpeedImpl(10)).asSeconds(), 0.1);
        assertEquals(458.7, allowancesPerCourse.get(new KnotSpeedImpl(12)).asSeconds(), 0.1);
        assertEquals(444.1, allowancesPerCourse.get(new KnotSpeedImpl(14)).asSeconds(), 0.1);
        assertEquals(430.3, allowancesPerCourse.get(new KnotSpeedImpl(16)).asSeconds(), 0.1);
        assertEquals(404.3, allowancesPerCourse.get(new KnotSpeedImpl(20)).asSeconds(), 0.1);
    }
    
    @Test
    public void testPerformanceCurveInvertation() throws MaxIterationsExceededException, FunctionEvaluationException {
        Double accuracy = 0.1;
        ORCCertificateImpl certificate = (ORCCertificateImpl) importer.getCertificate("GER 5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve(course);
        
        assertEquals( 11.5, performanceCurve.getImpliedWind(performanceCurve.getAllowancePerCourse(new KnotSpeedImpl( 11.5))).getKnots(), accuracy);
        assertEquals(17.23, performanceCurve.getImpliedWind(performanceCurve.getAllowancePerCourse(new KnotSpeedImpl(17.23))).getKnots(), accuracy);
        assertEquals(   18, performanceCurve.getImpliedWind(performanceCurve.getAllowancePerCourse(new KnotSpeedImpl(   18))).getKnots(), accuracy);
        
        assertEquals(  450, performanceCurve.getAllowancePerCourse(performanceCurve.getImpliedWind(Duration.ONE_SECOND.times(450))).asSeconds(), accuracy);
        assertEquals(  500, performanceCurve.getAllowancePerCourse(performanceCurve.getImpliedWind(Duration.ONE_SECOND.times(500))).asSeconds(), accuracy);
        assertEquals(  600, performanceCurve.getAllowancePerCourse(performanceCurve.getImpliedWind(Duration.ONE_SECOND.times(600))).asSeconds(), accuracy);
        assertEquals(  700, performanceCurve.getAllowancePerCourse(performanceCurve.getImpliedWind(Duration.ONE_SECOND.times(700))).asSeconds(), accuracy);
        assertEquals(  750, performanceCurve.getAllowancePerCourse(performanceCurve.getImpliedWind(Duration.ONE_SECOND.times(750))).asSeconds(), accuracy);
    }
    
    // Tests for a Implied Wind calculation for a simple predefined course. The solutions are extracted from the provided ORC TestPCS.exe application. 
    @Test
    public void testImpliedWindSimple() throws MaxIterationsExceededException, FunctionEvaluationException {
       Double accuracy = 0.000001;
       ORCCertificate certificateMoana  = importer.getCertificate("GER 5549");
       ORCCertificate certificateMilan  = importer.getCertificate("GER 7323");
       ORCCertificate certificateTutima = importer.getCertificate("GER 5609");
       ORCCertificate certificateBank   = importer.getCertificate("GER 5555");
       ORCCertificate certificateHaspa  = importer.getCertificate("GER 6300");
       ORCCertificate certificateHalbtrocken = importer.getCertificate("GER 5564");
       ORCPerformanceCurve performanceCurveMoana  = certificateMoana.getPerformanceCurve(course);
       ORCPerformanceCurve performanceCurveMilan  = certificateMilan.getPerformanceCurve(course);
       ORCPerformanceCurve performanceCurveTutima = certificateTutima.getPerformanceCurve(course);
       ORCPerformanceCurve performanceCurveBank   = certificateBank.getPerformanceCurve(course);
       ORCPerformanceCurve performanceCurveHaspa  = certificateHaspa.getPerformanceCurve(course);
       ORCPerformanceCurve performanceCurveHalbtrocken = certificateHalbtrocken.getPerformanceCurve(course);
       
       assertNotNull(performanceCurveMoana);
       assertNotNull(performanceCurveMilan);
       
       // Test for corner case and if the algorithm reacts to the boundaries of 6 and 20 kts.
       assertEquals(20.0    , performanceCurveMoana.getImpliedWind(Duration.ONE_HOUR).getKnots(), accuracy);
       
       assertEquals(12.89281, performanceCurveMilan .getImpliedWind(Duration.ONE_HOUR.times(1.0)).getKnots(), accuracy);
       assertEquals(8.72668 , performanceCurveTutima.getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(8.07591 , performanceCurveBank  .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.78413 , performanceCurveHaspa .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.76218 , performanceCurveMoana .getImpliedWind(Duration.ONE_HOUR.times(1.5)).getKnots(), accuracy);
       assertEquals(7.62407 , performanceCurveHalbtrocken.getImpliedWind(Duration.ONE_HOUR.times(2.0)).getKnots(), accuracy);
    }
    
}
