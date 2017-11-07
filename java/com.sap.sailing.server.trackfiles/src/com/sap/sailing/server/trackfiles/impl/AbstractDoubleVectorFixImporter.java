package com.sap.sailing.server.trackfiles.impl;

import com.sap.sailing.domain.trackimport.BaseDoubleVectorFixImporter;

/**
 * Abstract implementation of DoubleVectorFixImporter to import CSV data files from Bravo units.
 * 
 * @see BaseBravoDataImporterImpl
 *
 */
public abstract class AbstractDoubleVectorFixImporter implements BaseDoubleVectorFixImporter {
    private final String type;

    public AbstractDoubleVectorFixImporter(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}
