package com.sap.sailing.domain.ranking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.parser.ParseException;
import org.junit.Test;


public class TestORCCertificateImporterJSON {

    private static final String RESOURCES = "resources/orc/";
    
    @Test
    public void testSimpleLocalJSONFileRead () throws IOException, ParseException {
        File fileGER = new File(RESOURCES + "GER2019.json");
        ORCCertificateImporterJSON importer = new ORCCertificateImporterJSON(new FileInputStream(fileGER));
        
        ORCCertificate milan = importer.getCertificate(" ger 7323");
        assertNotNull(milan);
        assertEquals("19.812", milan.getValue("LOA"));
    }
    
    @Test
    public void testSimpleOnlineJSONFileRead () throws IOException, ParseException {
        ORCCertificateImporterJSON importer = new ORCCertificateImporterJSON(new URL("https://data.orc.org/public/WPub.dll?action=DownRMS&CountryId=GER&ext=json").openStream());
        
        ORCCertificate swan = importer.getCertificate(" GER 5335");
        assertNotNull(swan);
        assertEquals("NAUTOR", swan.getValue("Builder"));
    }
}
