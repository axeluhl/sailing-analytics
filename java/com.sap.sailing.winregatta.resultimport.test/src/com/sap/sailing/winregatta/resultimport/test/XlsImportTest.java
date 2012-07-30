package com.sap.sailing.winregatta.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.winregatta.resultimport.impl.CompetitorResultsXlsImporter;

public class XlsImportTest {
    private static final String SAMPLE_INPUT_NAME = "Erg_Drachen_Wannseewoche_2012_Beispiel.xlsx";
    private static final String RESOURCES = "resources/";

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    private InputStream getSampleInputStream() throws FileNotFoundException, IOException {
        return getInputStream(SAMPLE_INPUT_NAME);
    }

    @Test
    public void testLoadingSampleXLS() throws Exception {
    	CompetitorResultsXlsImporter resultlistFromXlsImporter = new CompetitorResultsXlsImporter();
    	RegattaResults regattaResults = resultlistFromXlsImporter.getRegattaResults(getSampleInputStream(), "Erg_Drachen");

    	assertNotNull(regattaResults);
    	assertNotNull(regattaResults.getMetadata());
    	assertNotNull(regattaResults.getCompetitorResults());
    	assertEquals(19, regattaResults.getCompetitorResults().size());
    	
    	Map<String, String> metadata = regattaResults.getMetadata();
    	assertNotNull(metadata.get("boatClass"));
    	assertEquals("Drachen", metadata.get("boatClass"));
    	assertNotNull(metadata.get("eventName"));
    	assertEquals("Wannsee Woche", metadata.get("eventName"));
    }
}
