package com.sap.sailing.kiworesultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sailing.kiworesultimport.ResultListParser;
import com.sap.sailing.kiworesultimport.ResultListParserFactory;

public class ParserTest {
    private static final String RESOURCES = "resources/";

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    @Test
    public void testLoadingSampleXML() throws SAXException, IOException, ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(getSampleInputStream());
        assertNotNull(doc);
        Node resultList = doc.getElementsByTagName("ResultList").item(0);
        assertNotNull(resultList);
    }

    private InputStream getSampleInputStream() throws FileNotFoundException, IOException {
        return getInputStream("2011-06-18_49er_Wettfahrt_2_Extra.xml");
    }
    
    @Test
    public void testObtainingResultList() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        ResultListParser parser = ResultListParserFactory.INSTANCE.createResultListParser();
        ResultList resultList = parser.parse(getSampleInputStream());
        assertNotNull(resultList);
        assertEquals("D:\\Programme\\KWSailing\\eventlogos\\KielerWoche_Ergebnislistenkopf_2011.jpg", resultList.getImagePfad());
        assertEquals(new String(new byte[] { (byte) 160  /* non-breaking space */}), resultList.getLegende());
    }
}
