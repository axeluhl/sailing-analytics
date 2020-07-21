package com.sap.sailing.declination.test;

import org.junit.Before;
import org.junit.Ignore;

import com.sap.sailing.declination.impl.NOAAImporter;

@Ignore("NOAA seems down as of 2020-07-22")
public class NOAASimpleDeclinationTest extends SimpleDeclinationTest<NOAAImporter> {
    @Before
    public void setUp() {
        importer = new NOAAImporter();
    }
}
