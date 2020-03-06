package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;

import com.sap.sailing.domain.common.orc.ORCCertificate;

public class TestORCCertificateImporterRMS {
    private static final String RESOURCES = "resources/orc/";
    
    @Rule
    public IgnoreInvalidOrcCertificatesRule customIgnoreRule = new IgnoreInvalidOrcCertificatesRule();
    
    @Test
    public void testSimpleLocalRMSFileRead() throws IOException, ParseException {
        File fileGER = new File(RESOURCES + "GER2019.rms");
        ORCCertificatesCollection certificates = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(fileGER));
        ORCCertificate milan = certificates.getCertificateById("GER166844GER7323");
        assertNotNull(milan);
    }
    
    @IgnoreInvalidOrcCertificates
    @Test
    public void testSimpleOnlineRMSFileRead() throws IOException, ParseException {
        Collection<ORCCertificate> certificates = customIgnoreRule.getAvailableCerts();
        final ORCCertificate cert = certificates.stream().findFirst().get();
        String countryCode = cert.getId().substring(0, 3);
        String url = String.format("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=%s&ext=json", countryCode);
        ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new URL(url).openStream());

        ORCCertificate swan = null, refernceCert = null;
        for (ORCCertificate orcCertificate : certificates) {
            swan = importer.getCertificateById(orcCertificate.getId());
            if (swan != null) {
                refernceCert = orcCertificate;
                break;
            }
        }

        assertNotNull(swan);
        assertNotNull(refernceCert);
        assertEquals(refernceCert.getGPHInSecondsToTheMile(), swan .getGPHInSecondsToTheMile(), 0.0000001);
//        assertEquals(
//              refernceCert.getWindwardLeewardSpeedPrediction().get(ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 
//              swan.getWindwardLeewardSpeedPrediction().get(ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(),
//              0.1);
//      assertEquals(
//              refernceCert.getLongDistanceSpeedPredictions().get(ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(),
//              swan.getLongDistanceSpeedPredictions().get(ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds(), 
//              0.1);
    }
    
    @Test
    public void testLocalRMSFileReadWithIdenticalSailnumbers() throws IOException, ParseException {
        //additional test to ensure certificates with same sailnumbers but different ids are equally parsed and saved
        File file = new File(RESOURCES + "multipleIdenticalSailnumbers.rms");
        ORCCertificatesCollection certificates = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(file));
        assertEquals(14, ((Collection<?>) (certificates.getCertificateIds())).size());
    }
}    
    
