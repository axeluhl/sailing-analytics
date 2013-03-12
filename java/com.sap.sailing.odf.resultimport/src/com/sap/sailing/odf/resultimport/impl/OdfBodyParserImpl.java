package com.sap.sailing.odf.resultimport.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sap.sailing.odf.resultimport.OdfBodyParser;
import com.sap.sailing.odf.resultimport.OdfBody;

public class OdfBodyParserImpl implements OdfBodyParser {

    private Document parseDocument(InputStream inputStream) throws SAXException, IOException, ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
    }

    @Override
    public OdfBody parse(InputStream inputStream, String sourceName) throws SAXException, IOException, ParserConfigurationException {
        Document doc = parseDocument(inputStream);
        OdfBody result = new OdfBodyImpl(doc.getElementsByTagName("OdfBody").item(0));
        return result;
    }

}
