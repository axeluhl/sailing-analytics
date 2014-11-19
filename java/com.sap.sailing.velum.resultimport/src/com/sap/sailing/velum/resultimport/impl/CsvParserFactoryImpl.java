package com.sap.sailing.velum.resultimport.impl;

import java.io.InputStream;

import com.sap.sailing.velum.resultimport.CsvParser;
import com.sap.sailing.velum.resultimport.CsvParserFactory;
import com.sap.sse.common.TimePoint;

public class CsvParserFactoryImpl implements CsvParserFactory {
    @Override
    public CsvParser createParser(InputStream is, String name, TimePoint lastModified) {
        return new CsvParserImpl(is, name, lastModified);
    }
}
