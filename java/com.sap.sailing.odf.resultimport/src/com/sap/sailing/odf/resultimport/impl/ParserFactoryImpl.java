package com.sap.sailing.odf.resultimport.impl;

import com.sap.sailing.odf.resultimport.ParserFactory;
import com.sap.sailing.odf.resultimport.ResultListParser;
import com.sap.sailing.odf.resultimport.StartReportParser;

public class ParserFactoryImpl implements ParserFactory {

    @Override
    public ResultListParser createResultListParser() {
        return new ResultListParserImpl();
    }

    @Override
    public StartReportParser createStartReportParser() {
        return new StartReportParserImpl();
    }
}
