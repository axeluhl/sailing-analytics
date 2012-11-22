package com.sap.sailing.kiworesultimport.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.sailing.kiworesultimport.ParserFactory;
import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sailing.kiworesultimport.StartReport;
import com.sap.sailing.kiworesultimport.ZipFile;
import com.sap.sailing.kiworesultimport.ZipFileParser;

public class ZipFileParserImpl implements ZipFileParser {
    private final ParserFactory parserFactory;
    
    public ZipFileParserImpl(ParserFactory parserFactory) {
        super();
        this.parserFactory = parserFactory;
    }

    @Override
    public ZipFile parse(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        ZipInputStream zis = new ZipInputStream(inputStream);
        List<StartReport> startReports = new ArrayList<StartReport>();
        List<ResultList> resultLists = new ArrayList<ResultList>();
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            if (isStartReport(entry)) {
                startReports.add(parserFactory.createStartReportParser().parse(getNonClosableInputStream(zis), entry.getName()));
            } else if (isResultList(entry)) {
                resultLists.add(parserFactory.createResultListParser().parse(getNonClosableInputStream(zis), entry.getName()));
            }
            entry = zis.getNextEntry();
        }
        return new ZipFileImpl(startReports, resultLists);
    }

    private InputStream getNonClosableInputStream(ZipInputStream zis) {
        return new FilterInputStream(zis) {
            @Override
            public void close() { }
        };
    }

    private boolean isResultList(ZipEntry entry) {
        return entry.getName().endsWith("_Extra.xml");
    }

    private boolean isStartReport(ZipEntry entry) {
        return entry.getName().startsWith("Startberichte\\");
    }

}
