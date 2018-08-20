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
    public AbstractBravoDataImporterImpl(String type, Map<String, Integer> columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix) {
        super(columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix, type);
    }
}
