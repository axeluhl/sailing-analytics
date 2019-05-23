package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.FunctionEvaluationException;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

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

    private static ORCPerformanceCurveCourse course;
    private static ORCCertificateImporter importer;

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
        // Moqutio? Or use just a normal Certificate from Import?
        Double accuracy = 0.21;
        ORCCertificateImpl certificate = (ORCCertificateImpl) importer.getCertificate("GER 5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve(course);
        Double value = performanceCurve.getDurationPerNauticalMileAtTrueWindAngleAndSpeed(new KnotSpeedImpl(6), new DegreeBearingImpl(60)).asSeconds();

        assertNotNull(course);
        assertNotNull(performanceCurve);
        assertEquals(value, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl( 6),new DegreeBearingImpl(  60)).asSeconds(), 0.000001);
        
        assertEquals(492.2, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl( 6),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(425.0, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl( 8),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(403.6, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(10),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(393.1, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(12),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(386.7, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(14),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(382.6, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(16),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        assertEquals(371.4, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(20),new DegreeBearingImpl(62.5)).asSeconds(), accuracy);
        
        assertEquals(483.6, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl( 6),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(418.7, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl( 8),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(394.8, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(10),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(377.9, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(12),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(360.2, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(14),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(345.0, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(16),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        assertEquals(321.7, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(20),new DegreeBearingImpl(98.3)).asSeconds(), accuracy);
        
        //assertEquals(537.3, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(6), new DegreeBearingImpl(50)).asSeconds(), accuracy);
        
        /*
         TESTS of the corner areas of the polars, currently not working
        assertEquals(588.1, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl( 6),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(468.4, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl( 8),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(413.8, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(10),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(382.6, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(12),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(355.1, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(14),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(326.4, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(16),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        assertEquals(275.4, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(20),new DegreeBearingImpl(138.7)).asSeconds(), accuracy);
        */
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
        
        assertNotNull(course);
        assertNotNull(performanceCurve);
        assertEquals(498.4, performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(6),new DegreeBearingImpl( 60)).asSeconds(), 0.0000000000001);
        assertEquals(506  , performanceCurve.getLagrangeInterpolationPerTrueWindSpeedAndAngle(new KnotSpeedImpl(6),new DegreeBearingImpl(120)).asSeconds(), 0.0000000000001);
        assertEquals(654.4, allowancesPerCourse.get(new KnotSpeedImpl( 6)).asSeconds(), 0.1);
        assertEquals(538.6, allowancesPerCourse.get(new KnotSpeedImpl( 8)).asSeconds(), 0.1);
        assertEquals(485.1, allowancesPerCourse.get(new KnotSpeedImpl(10)).asSeconds(), 0.1);
        assertEquals(458.7, allowancesPerCourse.get(new KnotSpeedImpl(12)).asSeconds(), 0.1);
        assertEquals(444.1, allowancesPerCourse.get(new KnotSpeedImpl(14)).asSeconds(), 0.1);
        assertEquals(430.3, allowancesPerCourse.get(new KnotSpeedImpl(16)).asSeconds(), 0.1);
        assertEquals(404.3, allowancesPerCourse.get(new KnotSpeedImpl(20)).asSeconds(), 0.1);
    }

    /*
    @Test
    public void testComplexConstructedCourse() throws FunctionEvaluationException {
        ORCCertificateImpl certificate = importer.getCertificate("GER 5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve();

        assertNotNull(certificate);
        assertNotNull(course);
        assertNotNull(performanceCurve);
        
        Map<Speed, Duration> allowancesPerCourse = performanceCurve.createAllowancesPerCourse(course);
        
        assertNotNull(allowancesPerCourse);
        assertEquals(446.8, allowancesPerCourse.get(new KnotSpeedImpl(20)).asSeconds(), 0.1);
        assertEquals(778.4, allowancesPerCourse.get(new KnotSpeedImpl( 6)).asSeconds(), 0.1);
        
        System.out.println(course.getTotalLength().getNauticalMiles());
        System.out.println(performanceCurve.createAllowancesPerCourse(course));
    }
    
    */

}
