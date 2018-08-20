package com.sap.sailing.kiworesultimport;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public interface ZipFileParser {
    ZipFile parse(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException;
}
