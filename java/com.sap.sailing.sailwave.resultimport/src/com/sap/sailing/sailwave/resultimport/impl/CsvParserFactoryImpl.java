package com.sap.sailing.sailwave.resultimport.impl;

import java.io.InputStream;

import com.sap.sailing.sailwave.resultimport.CsvParser;
import com.sap.sailing.sailwave.resultimport.CsvParserFactory;
import com.sap.sse.common.TimePoint;

public class CsvParserFactoryImpl implements CsvParserFactory {
    @Override
    public CsvParser createParser(InputStream is, String name, TimePoint lastModified) {
        return new CsvParserImpl(is, name, lastModified);
    }
}
