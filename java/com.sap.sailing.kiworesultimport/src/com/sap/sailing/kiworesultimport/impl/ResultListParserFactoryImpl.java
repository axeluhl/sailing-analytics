package com.sap.sailing.kiworesultimport.impl;

import com.sap.sailing.kiworesultimport.ResultListParser;
import com.sap.sailing.kiworesultimport.ResultListParserFactory;

public class ResultListParserFactoryImpl implements ResultListParserFactory {

    @Override
    public ResultListParser createResultListParser() {
        return new ResultListParserImpl();
    }

}
