package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.NOAAImporter;

public class NOAASimpleDeclinationTest extends SimpleDeclinationTest<NOAAImporter> {
    @Before
    public void setUp() {
        importer = new NOAAImporter();
    }
}
