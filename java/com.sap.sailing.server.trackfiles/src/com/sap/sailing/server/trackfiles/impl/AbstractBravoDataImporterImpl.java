package com.sap.sailing.server.trackfiles.impl;

import java.util.Map;

import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;

/**
 * Abstract implementation of DoubleVectorFixImporter to import CSV data files from Bravo units.
 * 
 * @see BaseBravoDataImporterImpl
 *
 */
public abstract class AbstractBravoDataImporterImpl extends BaseBravoDataImporterImpl implements DoubleVectorFixImporter {
    private final String type;

    public AbstractBravoDataImporterImpl(String type, Map<String, Integer> columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix) {
        super(columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix);
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}
