package com.sap.sailing.declination.test;

import org.junit.Before;
import org.junit.Ignore;

import com.sap.sailing.declination.impl.NOAAImporter;

@Ignore("US Government Shutdown around 2018-01-22")
public class NOAADeclinationServiceTest extends DeclinationServiceTest<NOAAImporter> {
    @Override
    @Before
    public void setUp() {
        importer = new NOAAImporter();
        super.setUp();
    }
}
