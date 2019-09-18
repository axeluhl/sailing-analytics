package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;


public class TestORCCertificateImporterJSON {
    private static final String RESOURCES = "resources/orc/";
    
    @Test
    public void testSimpleLocalJSONFileRead() throws IOException, ParseException {
        File fileGER = new File(RESOURCES + "GER2019.json");
        ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(fileGER));
        ORCCertificate milan = importer.getCertificateBySailNumber(" ger 7323");
        assertNotNull(milan);
    }
    
    @Test
    public void testSimpleOnlineJSONFileRead() throws IOException, ParseException {
        ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
        ORCCertificate swan  = importer.getCertificateBySailNumber(" GER 5335");
        ORCCertificate moana = importer.getCertificateBySailNumber("ger  55 49 ");
        assertNotNull(swan);
        assertNotNull(moana);
        assertEquals(539.1, swan .getGPH(), 0.0000001);
        assertEquals(490.4, moana.getGPH(), 0.0000001);
        assertEquals(862.2, swan .getWindwardLeewardSpeedPrediction().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 0.1);
        assertEquals(788.2, moana.getWindwardLeewardSpeedPrediction().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 0.1);
        assertEquals(861.0, swan .getLongDistanceSpeedPredictions().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 0.1);
        assertEquals(787.2, moana.getLongDistanceSpeedPredictions().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 0.1);
    }
}
