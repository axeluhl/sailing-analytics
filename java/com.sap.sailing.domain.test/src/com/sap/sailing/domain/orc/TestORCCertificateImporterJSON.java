package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sse.common.Util;


public class TestORCCertificateImporterJSON {
    private static final String RESOURCES = "resources/orc/";
    
    @Rule
    public FailIfNoValidOrcCertificateRule customIgnoreRule = new FailIfNoValidOrcCertificateRule();
    
    @Test
    public void testSimpleLocalJSONFileRead() throws IOException, ParseException {
        File fileGER = new File(RESOURCES + "GER2019.json");
        ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(fileGER));
        ORCCertificate milan = importer.getCertificateById("GER166844GER7323");
        assertNotNull(milan);
    }
    
    @FailIfNoValidOrcCertificates
    @Test
    public void testSimpleOnlineJSONFileRead() throws IOException, ParseException, InterruptedException {
        Collection<ORCCertificate> certificates = customIgnoreRule.getAvailableCerts();
        final ORCCertificate cert = certificates.stream().findFirst().get();
        String countryCode = cert.getId().substring(0, 3);
        String url = String.format("https://data.orc.org/public/WPub.dll?action=DownBoatRMS&CountryId=%s&ext=json", countryCode);
        ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new URL(url).openStream());
        ORCCertificate someValidCert = null, referenceCert = null;
        for (ORCCertificate orcCertificate : certificates) {
            someValidCert = importer.getCertificateById(orcCertificate.getId());
            if (someValidCert != null) {
                referenceCert = orcCertificate;
                break;
            }
        }
        assertNotNull("None of the certificates "+String.join(", ", Util.map(certificates, c->c.toString()))+
                " was found when importing the country document from "+url, someValidCert);
        assertNotNull(referenceCert);
        assertEquals(referenceCert.getGPHInSecondsToTheMile(), someValidCert.getGPHInSecondsToTheMile(), 0.0000001);
        assertTrue(referenceCert.getWindwardLeewardSpeedPrediction().get(ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds() > 10); 
        assertTrue(referenceCert.getLongDistanceSpeedPredictions().get(ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificateImpl.NAUTICAL_MILE).asSeconds() > 10);
    }
}
