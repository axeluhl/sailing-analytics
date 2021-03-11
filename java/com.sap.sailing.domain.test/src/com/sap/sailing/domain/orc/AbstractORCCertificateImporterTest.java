package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Rule;

import com.sap.sailing.domain.common.orc.ORCCertificate;


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
}
