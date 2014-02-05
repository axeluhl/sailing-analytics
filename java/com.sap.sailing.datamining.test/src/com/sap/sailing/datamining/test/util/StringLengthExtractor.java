package com.sap.sailing.datamining.test.util;

import com.sap.sailing.datamining.impl.AbstractExtractionWorker;

public class StringLengthExtractor extends AbstractExtractionWorker<String, Integer> {

    @Override
    public Integer extract(String dataEntry) {
        return dataEntry.length();
    }

}