package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import org.json.simple.parser.ParseException;
import org.junit.Test;

public class TestORCCertificateImporterRMS extends AbstractORCCertificateImporterTest {
    private static final String RESOURCES = "resources/orc/";
    
    @Test
    public void testSimpleLocalRMSFileRead() throws IOException, ParseException {
        testSimpleLocalFileRead("GER2019.rms", "GER166844GER7323");
    }
    
    @FailIfNoValidOrcCertificates
    @Test
    public void testSimpleOnlineRMSFileRead() throws IOException, ParseException, InterruptedException {
        testSimpleOnlineFileRead("rms");
    }
    
    @Test
    public void testLocalRMSFileReadWithIdenticalSailnumbers() throws IOException, ParseException {
        // additional test to ensure certificates with same sailnumbers but different ids are equally parsed and saved
        File file = new File(RESOURCES + "multipleIdenticalSailnumbers.rms");
        ORCCertificatesCollection certificates = ORCCertificatesImporter.INSTANCE.read(new FileInputStream(file));
        assertEquals(14, ((Collection<?>) (certificates.getCertificateIds())).size());
    }
}    
    
