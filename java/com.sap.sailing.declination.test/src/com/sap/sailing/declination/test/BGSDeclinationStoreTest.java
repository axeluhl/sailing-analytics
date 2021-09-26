package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.BGSImporter;

public class BGSDeclinationStoreTest extends DeclinationStoreTest<BGSImporter> {
    @Override
    @Before
    public void setUp() {
        importer = new BGSImporter();
        super.setUp();
    }
}
