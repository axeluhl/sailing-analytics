package com.sap.sailing.winregatta.resultimport.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.winregatta.resultimport.impl.BarbadosResultSpreadsheet;

public class BarbadosResultImportTest {
    private static final String SAMPLE_INPUT_NAME_EMPTY_RESULTS = "RESULTS-505Barbados.xlsx";
    private static final String RESOURCES = "resources/";
    private BarbadosResultSpreadsheet spreadsheet;

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    @Before
    public void setUp() throws FileNotFoundException, IOException, Exception {
        spreadsheet = new BarbadosResultSpreadsheet(getInputStream(SAMPLE_INPUT_NAME_EMPTY_RESULTS));
    }
    
    @Test
    public void testOpenDocument() {
        assertNotNull(spreadsheet);
        RegattaResults regattaResults = spreadsheet.getRegattaResults();
        assertNotNull(regattaResults);
        assertEquals("505", regattaResults.getMetadata().get("boatclassName"));
    }

    private void assertEquals(String string, String string2) {
        // TODO Auto-generated method stub
        
    }
}
