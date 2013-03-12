package com.sap.sailing.odf.resultimport.test;

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

import com.sap.sailing.odf.resultimport.Competition;
import com.sap.sailing.odf.resultimport.CumulativeResult;
import com.sap.sailing.odf.resultimport.OdfBody;
import com.sap.sailing.odf.resultimport.OdfBodyParser;
import com.sap.sailing.odf.resultimport.ParserFactory;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME = "SAM004000_DT_CUMULATIVE_RESULT_SAM004000_1.0.xml";
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
        Node resultList = doc.getElementsByTagName("OdfBody").item(0);
        assertNotNull(resultList);
    }

    private InputStream getSampleInputStream() throws FileNotFoundException, IOException {
        return getInputStream(SAMPLE_INPUT_NAME);
    }
    
    @Test
    public void testEmptyStatus() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        OdfBodyParser parser = ParserFactory.INSTANCE.createOdfBodyParser();
        OdfBody body = parser.parse(getSampleInputStream(), SAMPLE_INPUT_NAME);
        assertNotNull(body);
        Iterable<Competition> competitions = body.getCompetitions();
        for (Competition competition : competitions) {
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                System.out.println(cumulativeResult);
            }
        }
    }
    
}
