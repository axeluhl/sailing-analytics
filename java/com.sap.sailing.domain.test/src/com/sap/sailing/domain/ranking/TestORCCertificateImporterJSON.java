package com.sap.sailing.domain.ranking;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.ranking.ORCCertificateImporterJSON;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class TestORCCertificateImporterJSON {

    private static final String RESOURCES = "resources/orc/";
    
    @Test
    public void testSimpleJSONFileRead () throws FileNotFoundException, IOException, ParseException {
        File fileGER = new File(RESOURCES + "GER2019.json");
        ORCCertificateImporterJSON importer = new ORCCertificateImporterJSON(new FileInputStream(fileGER));
        
        ORCCertificate milan = importer.getCertificate(" ger 7323");
        assertNotNull(milan);
        assertEquals("19.812", milan.getValue("LOA"));
    }
}
