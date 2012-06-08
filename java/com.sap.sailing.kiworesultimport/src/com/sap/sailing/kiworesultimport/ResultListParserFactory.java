package com.sap.sailing.kiworesultimport;

import com.sap.sailing.kiworesultimport.impl.ResultListParserFactoryImpl;

public interface ResultListParserFactory {
    ResultListParserFactory INSTANCE = new ResultListParserFactoryImpl();
    
    ResultListParser createResultListParser();
}
