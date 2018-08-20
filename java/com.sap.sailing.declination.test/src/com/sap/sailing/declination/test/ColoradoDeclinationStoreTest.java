package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.ColoradoImporter;

public class ColoradoDeclinationStoreTest extends DeclinationStoreTest<ColoradoImporter> {
    @Override
    @Before
    public void setUp() {
        importer = new ColoradoImporter();
        super.setUp();
    }
}
