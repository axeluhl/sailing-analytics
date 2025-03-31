package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.BGSImporter;

public class BGSSimpleDeclinationTest extends SimpleDeclinationTest<BGSImporter> {
    @Before
    public void setUp() {
        importer = new BGSImporter();
    }
}
