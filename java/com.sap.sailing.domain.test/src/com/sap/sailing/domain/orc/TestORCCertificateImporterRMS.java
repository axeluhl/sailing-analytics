package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sailing.domain.orc.impl.ORCCertificateImporterRMS;

public class TestORCCertificateImporterRMS {
    private static final String RESOURCES = "resources/orc/";

    @Test
    public void testSimpleLocalRMSFileRead() throws IOException {
        File fileGER = new File(RESOURCES + "GER2019.rms");
        ORCCertificateImporter importer = new ORCCertificateImporterRMS(new FileInputStream(fileGER));
        ORCCertificate milan = importer.getCertificate(" ger 7323");
        assertNotNull(milan);
    }
    
    @Test
    public void testSimpleOnlineRMSFileRead() throws IOException {
        ORCCertificateImporter importer = new ORCCertificateImporterRMS(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=rms").openStream());
        ORCCertificate swan  = importer.getCertificate(" GER 5335");
        ORCCertificate moana = importer.getCertificate("ger  55 49 ");
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
    
