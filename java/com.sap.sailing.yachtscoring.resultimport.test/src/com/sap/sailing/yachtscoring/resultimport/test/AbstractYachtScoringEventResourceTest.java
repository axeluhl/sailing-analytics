package com.sap.sailing.yachtscoring.resultimport.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AbstractYachtScoringEventResourceTest {
    private static final String RESOURCES = "resources/";

    protected InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    protected File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

}
