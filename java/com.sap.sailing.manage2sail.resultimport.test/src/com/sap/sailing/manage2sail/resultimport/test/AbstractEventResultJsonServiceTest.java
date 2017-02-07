package com.sap.sailing.manage2sail.resultimport.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AbstractEventResultJsonServiceTest {
    protected static final String EVENT_RESULTS_JSON = "eventResults.json";
    
    protected static final String RESOURCES = "resources/";

    protected InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }
}
