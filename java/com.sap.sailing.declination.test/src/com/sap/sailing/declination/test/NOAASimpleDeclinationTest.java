package com.sap.sailing.declination.test;

import org.junit.Before;
import org.junit.Ignore;

import com.sap.sailing.declination.impl.NOAAImporter;

@Ignore("Due to scheduled maintenance, many NCEI systems will be unavailable February 4th, 12:00 PM ET - February 6th, 8:00 PM ET. (2025)")
public class NOAASimpleDeclinationTest extends SimpleDeclinationTest<NOAAImporter> {
    @Before
    public void setUp() {
        importer = new NOAAImporter();
    }
}
