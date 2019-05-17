package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.orc.impl.ORCCertificateImpl;
import com.sap.sailing.domain.orc.impl.ORCCertificateImporterJSON;


public class TestORCCertificateImporterJSON {
    private static final String RESOURCES = "resources/orc/";
    
    @Test
    public void testSimpleLocalJSONFileRead () throws IOException, ParseException {
        File fileGER = new File(RESOURCES + "GER2019.json");
        ORCCertificateImporter importer = new ORCCertificateImporterJSON(new FileInputStream(fileGER));
        ORCCertificate milan = importer.getCertificate(" ger 7323");
        assertNotNull(milan);
        //assertEquals("19.812", milan.getValueString("LOA"));
    }
    
    @Test
    public void testSimpleOnlineJSONFileRead () throws IOException, ParseException {
        ORCCertificateImporter importer = new ORCCertificateImporterJSON(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
        ORCCertificate swan  = importer.getCertificate(" GER 5335");
        ORCCertificate moana = importer.getCertificate("ger  55 49 ");
        assertNotNull(swan);
        assertNotNull(moana);
        assertEquals(539.1, swan .getGPH(), 0.0000001);
        assertEquals(490.4, moana.getGPH(), 0.0000001);
        assertEquals(862.2, swan .getWindwardLeewardAllowances().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).asSeconds(), 0.1);
        assertEquals(788.2, moana.getWindwardLeewardAllowances().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).asSeconds(), 0.1);
        assertEquals(861.0, swan .getLongDistanceAllowances().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).asSeconds(), 0.1);
        assertEquals(787.2, moana.getLongDistanceAllowances().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).asSeconds(), 0.1);
    }
}
