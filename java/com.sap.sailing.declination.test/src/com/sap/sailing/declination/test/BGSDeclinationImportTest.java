package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.BGSImporter;

public class BGSDeclinationImportTest extends DeclinationImportTest<BGSImporter> {
    @Before
    public void setUp() {
        importer = new BGSImporter();
    }
}
