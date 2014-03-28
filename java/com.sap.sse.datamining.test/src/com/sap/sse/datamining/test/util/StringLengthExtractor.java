package com.sap.sse.datamining.test.util;

import com.sap.sse.datamining.impl.workers.extractors.AbstractExtractionWorker;

public class StringLengthExtractor extends AbstractExtractionWorker<String, Integer> {

    @Override
    public Integer extract(String dataEntry) {
        return dataEntry.length();
    }

}