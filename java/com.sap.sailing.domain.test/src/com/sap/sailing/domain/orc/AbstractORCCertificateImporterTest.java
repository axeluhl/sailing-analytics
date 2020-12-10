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

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.Util;


public abstract class AbstractORCCertificateImporterTest {
    protected static final String RESOURCES = "resources/orc/";
    
    @Rule
    public FailIfNoValidOrcCertificateRule customIgnoreRule = new FailIfNoValidOrcCertificateRule();
    
    protected void testSimpleLocalFileRead(String fileName, String expectedCertificateId) throws IOException, ParseException {
        File fileGER = new File(RESOURCES + fileName);
        ORCCertificatesCollection importer = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(fileGER));
        ORCCertificate milan = importer.getCertificateById(expectedCertificateId);
        assertNotNull(milan);
    }
    
    @FailIfNoValidOrcCertificates
    protected void testSimpleOnlineFileRead(String rmsOrJsonFormat) throws IOException, ParseException, InterruptedException {
        Collection<ORCCertificate> certificates = customIgnoreRule.getAvailableCerts();
        final ORCCertificate cert = certificates.stream().findFirst().get();
        String countryCode = cert.getIssuingCountry().getThreeLetterIOCCode();
        String url = String.format("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=%s&ext=%s", countryCode, rmsOrJsonFormat);
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
        assertTrue(referenceCert.getWindwardLeewardSpeedPrediction().get(ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificate.NAUTICAL_MILE).asSeconds() > 10); 
        assertTrue(referenceCert.getLongDistanceSpeedPredictions().get(ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS[0]).getDuration(ORCCertificate.NAUTICAL_MILE).asSeconds() > 10);
    }
}
