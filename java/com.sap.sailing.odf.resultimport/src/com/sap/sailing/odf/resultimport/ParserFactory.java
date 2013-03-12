package com.sap.sailing.odf.resultimport;

import com.sap.sailing.odf.resultimport.impl.ParserFactoryImpl;

public interface ParserFactory {
    ParserFactory INSTANCE = new ParserFactoryImpl();
    
    OdfBodyParser createOdfBodyParser();
}
