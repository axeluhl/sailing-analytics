package com.sap.sailing.sailwave.resultimport;

import java.io.InputStream;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.sailwave.resultimport.impl.CsvParserFactoryImpl;

public interface CsvParserFactory {
    CsvParserFactory INSTANCE = new CsvParserFactoryImpl();
    CsvParser createParser(InputStream is, String name, TimePoint lastModified);
}
