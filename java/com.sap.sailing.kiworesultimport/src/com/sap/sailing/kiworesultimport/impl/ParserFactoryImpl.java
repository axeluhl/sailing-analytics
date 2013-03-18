package com.sap.sailing.kiworesultimport.impl;

import com.sap.sailing.kiworesultimport.ParserFactory;
import com.sap.sailing.kiworesultimport.ResultListParser;
import com.sap.sailing.kiworesultimport.StartReportParser;
import com.sap.sailing.kiworesultimport.ZipFileParser;

public class ParserFactoryImpl implements ParserFactory {

    @Override
    public ResultListParser createResultListParser() {
        return new ResultListParserImpl();
    }

    @Override
    public StartReportParser createStartReportParser() {
        return new StartReportParserImpl();
    }

    @Override
    public ZipFileParser createZipFileParser() {
        return new ZipFileParserImpl(this);
    }
}
