package com.sap.sailing.winregatta.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.resultimport.CompetitorEntry;
import com.sap.sailing.resultimport.CompetitorRow;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.winregatta.resultimport.impl.CompetitorResultsXlsImporter;

public class XlsImportTest {
    private static final String RESOURCES = "resources/";
    private static final String SAMPLE1_NAME = "Erg_Drachen_Wannseewoche_2012_Beispiel.xlsx";
    private static final String SAMPLE2_NAME = "Erg_505er.xlsx";
    private static final String SAMPLE3_NAME = "Erg_Drachen_IDM_2012.xlsx";

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    private InputStream getSample1InputStream() throws FileNotFoundException, IOException {
        return getInputStream(SAMPLE1_NAME);
    }

    private InputStream getSample2InputStream() throws FileNotFoundException, IOException {
        return getInputStream(SAMPLE2_NAME);
    }

    private InputStream getSample3InputStream() throws FileNotFoundException, IOException {
        return getInputStream(SAMPLE3_NAME);
    }

    @Test
    public void testLoadingSampleXLS() throws Exception {
    	CompetitorResultsXlsImporter resultlistFromXlsImporter = new CompetitorResultsXlsImporter();
    	
    	InputStream sample1InputStream = getSample1InputStream();
    	RegattaResults regattaResults = resultlistFromXlsImporter.getRegattaResults(sample1InputStream,
    			CompetitorResultsXlsImporter.IMPORT_TEMPLATE_WITH_RANKS_DRACHEN, "Erg_Drachen");

    	assertNotNull(regattaResults);
    	assertNotNull(regattaResults.getMetadata());
    	assertNotNull(regattaResults.getCompetitorResults());
    	assertEquals(19, regattaResults.getCompetitorResults().size());
    	
    	Map<String, String> metadata = regattaResults.getMetadata();
    	assertNotNull(metadata.get("boatClass"));
    	assertEquals("Drachen", metadata.get("boatClass"));
    	assertNotNull(metadata.get("eventName"));
    	assertEquals("Wannsee Woche", metadata.get("eventName"));
    	
    	sample1InputStream.close();
    }

    @Test
    public void testLoadingSampleWithDiscardingsXLS() throws Exception {
    	CompetitorResultsXlsImporter resultlistFromXlsImporter = new CompetitorResultsXlsImporter();
    	InputStream sample2InputStream = getSample2InputStream();
    	RegattaResults regattaResults = resultlistFromXlsImporter.getRegattaResults(sample2InputStream,
    			CompetitorResultsXlsImporter.IMPORT_TEMPLATE_505, "Erg_505er");
    	
    	assertNotNull(regattaResults);
    	assertNotNull(regattaResults.getMetadata());
    	assertNotNull(regattaResults.getCompetitorResults());

    	assertEquals(33, regattaResults.getCompetitorResults().size());

    	boolean oneRowWithInvalidDiscarding = false;
    	for(CompetitorRow row: regattaResults.getCompetitorResults()) {
    		Iterable<CompetitorEntry> entries = row.getRankAndMaxPointsReasonAndPointsAndDiscarded();
    		int discardCountPerRow = 0;
    		for(CompetitorEntry entry: entries) {
    			if( entry.isDiscarded()) {
    				discardCountPerRow++;
    			}
    		}
    		if(discardCountPerRow != 1) {
    			oneRowWithInvalidDiscarding = true;
    		}
    	}
    	assertFalse(oneRowWithInvalidDiscarding);
    	
    	Map<String, String> metadata = regattaResults.getMetadata();
    	assertNotNull(metadata.get("boatClass"));
    	assertEquals("505er", metadata.get("boatClass"));
    	
    	sample2InputStream.close();
    }

    @Test
    public void testLoadingDrachenIDM2012XLS() throws Exception {
    	CompetitorResultsXlsImporter resultlistFromXlsImporter = new CompetitorResultsXlsImporter();
    	InputStream sample3InputStream = getSample3InputStream();
    	RegattaResults regattaResults = resultlistFromXlsImporter.getRegattaResults(sample3InputStream,
    			CompetitorResultsXlsImporter.IMPORT_TEMPLATE_WITHOUT_RANKS_DRACHEN, "Erg_Drachen");
    	
    	assertNotNull(regattaResults);
    	assertNotNull(regattaResults.getMetadata());
    	assertNotNull(regattaResults.getCompetitorResults());

    	assertEquals(43, regattaResults.getCompetitorResults().size());
    	
    	Map<String, String> metadata = regattaResults.getMetadata();
    	assertNotNull(metadata.get("boatClass"));
    	assertEquals("Drachen", metadata.get("boatClass"));
    	
    	sample3InputStream.close();
    }
}
