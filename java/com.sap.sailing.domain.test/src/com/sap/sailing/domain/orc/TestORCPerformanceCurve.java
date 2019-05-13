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

    /*
    
    @Test
    public void testLagrangeInterpolation() throws FunctionEvaluationException {
        // Moqutio? Or use just a normal Certificate from Import?
        ORCCertificateImpl certificate = importer.getCertificate("GER 5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve();
        Double value = performanceCurve.getDurationPerNauticalMileAtTrueWindAngleAndSpeed(new KnotSpeedImpl(6), new DegreeBearingImpl(60)).asSeconds();

        assertNotNull(course);
        assertNotNull(performanceCurve);
        assertNotNull(performanceCurve.getLagrangeInterpolationPerTrueWindSpeed(new KnotSpeedImpl(6)));
        assertEquals(value, performanceCurve.getLagrangeInterpolationPerTrueWindSpeed(new KnotSpeedImpl(6)).value(60), 0.0000000000001);
    }
    
    @Test
    public void testSimpleConstructedCourse() throws FunctionEvaluationException {
        ORCCertificateImpl certificate = importer.getCertificate("GER 5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve();
        List<ORCPerformanceCurveLeg> legs = new ArrayList<>();
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(0)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(30)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(60)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(120)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.0), new DegreeBearingImpl(180)));
        ORCPerformanceCurveCourse simpleCourse = new ORCPerformanceCurveCourseImpl(legs);
        Map<Speed, Duration> allowancesPerCourse = performanceCurve.createAllowancesPerCourse(simpleCourse);
        
        assertNotNull(course);
        assertNotNull(performanceCurve);
        assertEquals(498.4, performanceCurve.getLagrangeInterpolationPerTrueWindSpeed(new KnotSpeedImpl(6)).value( 60), 0.0000000000001);
        assertEquals(506  , performanceCurve.getLagrangeInterpolationPerTrueWindSpeed(new KnotSpeedImpl(6)).value(120), 0.0000000000001);
        assertEquals(654.4, allowancesPerCourse.get(new KnotSpeedImpl( 6)).asSeconds(), 0.1);
        assertEquals(538.6, allowancesPerCourse.get(new KnotSpeedImpl( 8)).asSeconds(), 0.1);
        assertEquals(485.1, allowancesPerCourse.get(new KnotSpeedImpl(10)).asSeconds(), 0.1);
        assertEquals(458.7, allowancesPerCourse.get(new KnotSpeedImpl(12)).asSeconds(), 0.1);
        assertEquals(444.1, allowancesPerCourse.get(new KnotSpeedImpl(14)).asSeconds(), 0.1);
        assertEquals(430.3, allowancesPerCourse.get(new KnotSpeedImpl(16)).asSeconds(), 0.1);
        assertEquals(404.3, allowancesPerCourse.get(new KnotSpeedImpl(20)).asSeconds(), 0.1);
    }

    
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
