package com.sap.sailing.xrr.resultimport.impl;

import java.io.InputStream;

import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.ParserFactory;

public class ParserFactoryImpl implements ParserFactory {
    @Override
    public Parser createParser(InputStream is) {
        return new ParserImpl(is);
    }
}
