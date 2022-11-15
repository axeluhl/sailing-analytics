package com.sap.sailing.declination.test;

import org.junit.Before;
import org.junit.Ignore;

import com.sap.sailing.declination.impl.ColoradoImporter;

@Ignore("currently, http://magcalc.geomag.info/ seems down")
public class ColoradoSimpleDeclinationTest extends SimpleDeclinationTest<ColoradoImporter> {
    @Before
    public void setUp() {
        importer = new ColoradoImporter();
    }
}
