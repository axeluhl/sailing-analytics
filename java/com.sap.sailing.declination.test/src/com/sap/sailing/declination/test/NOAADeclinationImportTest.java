package com.sap.sailing.declination.test;

import org.junit.Before;
import org.junit.Ignore;

import com.sap.sailing.declination.impl.NOAAImporterForTesting;

@Ignore("US Government Shutdown around 2018-01-22")
public class NOAADeclinationImportTest extends DeclinationImportTest<NOAAImporterForTesting> {
    @Before
    public void setUp() {
        importer = new NOAAImporterForTesting();
    }
}
