package com.sap.sailing.odf.resultimport.impl;

import com.sap.sailing.odf.resultimport.OdfBodyParser;
import com.sap.sailing.odf.resultimport.ParserFactory;

public class ParserFactoryImpl implements ParserFactory {

    @Override
    public OdfBodyParser createOdfBodyParser() {
        return new OdfBodyParserImpl();
    }
}
