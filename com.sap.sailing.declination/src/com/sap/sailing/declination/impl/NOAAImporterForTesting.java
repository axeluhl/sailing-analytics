package com.sap.sailing.declination.impl;

import java.util.regex.Pattern;

import com.sap.sailing.declination.NOAAImporter;

public class NOAAImporterForTesting extends NOAAImporter {

    @Override
    public Pattern getDeclinationPattern() {
        return super.getDeclinationPattern();
    }

    @Override
    public Pattern getAnnualChangePattern() {
        return super.getAnnualChangePattern();
    }

}
