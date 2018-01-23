package com.sap.sailing.declination.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.declination.impl.ColoradoImporter;

public class ColoradoTest {
    @Test
    public void simpleDocumentParsingTest() throws SAXException, IOException, ParserConfigurationException {
        final Declination declination = new ColoradoImporter().getDeclinationFromXml(getClass().getResourceAsStream("/colorado.xml"));
        assertEquals(10.0, declination.getPosition().getLatDeg(), 0.000001);
        assertEquals(20.0, declination.getPosition().getLngDeg(), 0.000001);
        assertEquals(1.89255, declination.getBearing().getDegrees(), 0.000001);
        assertEquals(0.10649, declination.getAnnualChange().getDegrees(), 0.000001);
    }
}
