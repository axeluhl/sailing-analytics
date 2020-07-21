package com.sap.sailing.declination.test;

import org.junit.Before;
import org.junit.Ignore;

import com.sap.sailing.declination.impl.NOAAImporterForTesting;

@Ignore("NOAA seems down as of 2020-07-22")
public class NOAADeclinationImportTest extends DeclinationImportTest<NOAAImporterForTesting> {
    @Before
    public void setUp() {
        importer = new NOAAImporterForTesting();
    }
}
