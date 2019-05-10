package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.orc.impl.ORCCertificateImporterJSON;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveCourseImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sse.common.Distance;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class TestORCPerformanceCurve {

    private static ORCPerformanceCurveCourse course = null;

    @BeforeClass
    public static void initialize() {
        List<ORCPerformanceCurveLeg> legs = new ArrayList<>();
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2.23), new DegreeBearingImpl(10)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2.00), new DegreeBearingImpl(170)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(0.97), new DegreeBearingImpl(0)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.03), new DegreeBearingImpl(15)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.03), new DegreeBearingImpl(165)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.17), new DegreeBearingImpl(180)));
        course = new ORCPerformanceCurveCourseImpl(legs);
    }

    @Test
    public void testLagrangeInterpolation() throws IOException, ParseException, FunctionEvaluationException {
        // Moqutio? Or use just a normal Certificate from Import?
        ORCCertificateImporter importer = new ORCCertificateImporterJSON(
                new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
        ORCCertificate certificate = importer.getCertificate("GER 5549");
        ORCPerformanceCurveImpl performanceCurve = (ORCPerformanceCurveImpl) certificate.getPerformanceCurve();
        Double value = performanceCurve.getDurationPerNauticalMileAtTrueWindAngleAndSpeed(new KnotSpeedImpl(6), new DegreeBearingImpl(60)).asSeconds();

        assertNotNull(course);
        assertNotNull(performanceCurve);
        assertNotNull(performanceCurve.getLagrangeInterpolationPerTrueWindSpeed(new KnotSpeedImpl(6)));
        assertEquals(value, performanceCurve.getLagrangeInterpolationPerTrueWindSpeed(new KnotSpeedImpl(6)).value(60), 0.0000000000001);
    }

    @Test
    public void testConstructedCourse() throws IOException, ParseException {
        ORCCertificateImporter importer = new ORCCertificateImporterJSON(
                new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
        ORCCertificate certificate = importer.getCertificate("GER 5549");
        ORCPerformanceCurve performanceCurve = certificate.getPerformanceCurve();

        assertNotNull(certificate);
        assertNotNull(course);
        assertNotNull(performanceCurve);
    }

}
