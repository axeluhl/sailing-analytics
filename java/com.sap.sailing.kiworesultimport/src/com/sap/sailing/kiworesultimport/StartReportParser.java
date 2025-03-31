package com.sap.sailing.kiworesultimport;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public interface StartReportParser {
    StartReport parse(InputStream inputStream, String sourceName) throws SAXException, IOException, ParserConfigurationException;
}
