package com.sap.sailing.domain.orc;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Test;


public class TestORCCertificateImporterJSON extends AbstractORCCertificateImporterTest {
    
    @Test
    public void testSimpleLocalJSONFileRead() throws IOException, ParseException {
        testSimpleLocalFileRead("GER2019.json", "GER20041179");
    }
    
    @FailIfNoValidOrcCertificates
    @Test
    public void testSimpleOnlineJSONFileRead() throws IOException, ParseException, InterruptedException {
        testSimpleOnlineFileRead("json");
    }
}
