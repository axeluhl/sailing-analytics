package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.ColoradoImporter;

public class ColoradoDeclinationServiceTest extends DeclinationServiceTest<ColoradoImporter> {
    @Override
    @Before
    public void setUp() {
        importer = new ColoradoImporter();
        super.setUp();
    }
}
