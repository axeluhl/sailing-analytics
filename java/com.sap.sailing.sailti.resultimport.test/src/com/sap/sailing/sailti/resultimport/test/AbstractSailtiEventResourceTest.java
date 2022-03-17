package com.sap.sailing.sailti.resultimport.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.sailti.resultimport.UrlResultDocumentDescriptorImpl;

public class AbstractSailtiEventResourceTest {
    private static final String RESOURCES = "resources/";

    protected InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return UrlResultDocumentDescriptorImpl.getInputStreamReplacingRaceNumberByInteger(new FileInputStream(getFile(filename)));
    }

    protected File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

}
