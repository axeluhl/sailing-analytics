package com.sap.sailing.kiworesultimport.impl;

import com.sap.sailing.kiworesultimport.ParserFactory;
import com.sap.sailing.kiworesultimport.ResultListParser;
import com.sap.sailing.kiworesultimport.StartberichtParser;
import com.sap.sailing.kiworesultimport.ZipFileParser;

public class ParserFactoryImpl implements ParserFactory {

    @Override
    public ResultListParser createResultListParser() {
        return new ResultListParserImpl();
    }

    @Override
    public StartberichtParser createStartberichtParser() {
        return new StartberichtParserImpl();
    }

    @Override
    public ZipFileParser createZipFileParser() {
        return new ZipFileParserImpl();
    }
}
