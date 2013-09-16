package com.sap.sailing.declination.test;

import java.text.SimpleDateFormat;

import org.junit.Before;

import com.sap.sailing.declination.impl.NOAAImporterForTesting;

public abstract class AbstractDeclinationTest {
    protected NOAAImporterForTesting importer;
    protected final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    @Before
    public void setUp() {
        importer = new NOAAImporterForTesting();
    }
    
}
