package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.NOAAImporterForTesting;

public class NOAADeclinationImportTest extends DeclinationImportTest<NOAAImporterForTesting> {
    @Before
    public void setUp() {
        importer = new NOAAImporterForTesting();
    }
}
