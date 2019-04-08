package com.sap.sailing.declination.test;

import org.junit.Before;

import com.sap.sailing.declination.impl.ColoradoImporter;

public class ColoradoSimpleDeclinationTest extends SimpleDeclinationTest<ColoradoImporter> {
    @Before
    public void setUp() {
        importer = new ColoradoImporter();
    }
}
