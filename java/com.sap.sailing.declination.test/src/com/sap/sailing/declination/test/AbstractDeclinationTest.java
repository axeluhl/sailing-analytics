package com.sap.sailing.declination.test;

import java.text.SimpleDateFormat;

import com.sap.sailing.declination.impl.DeclinationImporter;

public abstract class AbstractDeclinationTest<I extends DeclinationImporter> {
    protected I importer;
    protected final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
}
