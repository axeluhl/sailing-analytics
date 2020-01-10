package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;

public class TestORCCertificateImporterRMS {
    private static final String RESOURCES = "resources/orc/";

    @Test
    public void testSimpleLocalRMSFileRead() throws IOException, ParseException {
        File fileGER = new File(RESOURCES + "GER2019.rms");
        ORCCertificatesCollection certificates = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(fileGER));
        ORCCertificate milan = certificates.getCertificateById("GER166844GER7323");
        assertNotNull(milan);
    }
    
    @Ignore("Certificate used for testing no longer valid after 2019")
    @Test
    public void testSimpleOnlineRMSFileRead() throws IOException, ParseException {
        ORCCertificatesCollection certificates = ORCCertificatesImporter.INSTANCE.read(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=rms").openStream());
        ORCCertificate swan  = certificates.getCertificateById("GER140849GER5335");
        ORCCertificate moana = certificates.getCertificateById("GER140772GER5549");
        assertNotNull(swan);
        assertNotNull(moana);
        assertEquals(539.1, swan .getGPHInSecondsToTheMile(), 0.0000001);
        assertEquals(490.4, moana.getGPHInSecondsToTheMile(), 0.0000001);
        assertEquals(862.2, swan .getWindwardLeewardSpeedPrediction().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 0.1);
        assertEquals(788.2, moana.getWindwardLeewardSpeedPrediction().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 0.1);
        assertEquals(861.0, swan .getLongDistanceSpeedPredictions().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 0.1);
        assertEquals(787.2, moana.getLongDistanceSpeedPredictions().get(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 0.1);
    }
    
    @Test
    public void testLocalRMSFileReadWithIdenticalSailnumbers() throws IOException, ParseException {
        //additional test to ensure certificates with same sailnumbers but different ids are equally parsed and saved
        File file = new File(RESOURCES + "multipleIdenticalSailnumbers.rms");
        ORCCertificatesCollection certificates = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(file));
        assertEquals(14, ((Collection<?>) (certificates.getCertificateIds())).size());
    }
}    
    
