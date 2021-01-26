package com.sap.sailing.declination.test;

import org.junit.Before;
import org.junit.Ignore;

import com.sap.sailing.declination.impl.NOAAImporter;

@Ignore("2021-01-20: Due to scheduled system maintenance, ngdc.noaa.gov services will be unavailable from Wednesday 2021-01-20 16:00 MT until Friday 2021-01-22 12:00 MT.")
public class NOAASimpleDeclinationTest extends SimpleDeclinationTest<NOAAImporter> {
    @Before
    public void setUp() {
        importer = new NOAAImporter();
    }
}
