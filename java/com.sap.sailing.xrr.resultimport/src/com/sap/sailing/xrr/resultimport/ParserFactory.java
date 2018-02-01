package com.sap.sailing.xrr.resultimport;

import java.io.InputStream;

import com.sap.sailing.xrr.resultimport.impl.ParserFactoryImpl;

public interface ParserFactory {
    ParserFactory INSTANCE = new ParserFactoryImpl();
    Parser createParser(InputStream is, String name);
}
