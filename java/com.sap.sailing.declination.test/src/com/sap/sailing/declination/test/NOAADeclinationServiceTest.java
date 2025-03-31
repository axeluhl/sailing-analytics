package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.NOAAImporter;

public class NOAADeclinationServiceTest extends DeclinationServiceTest<NOAAImporter> {
    @Override
    @Before
    public void setUp() {
        importer = new NOAAImporter();
        super.setUp();
    }
}
