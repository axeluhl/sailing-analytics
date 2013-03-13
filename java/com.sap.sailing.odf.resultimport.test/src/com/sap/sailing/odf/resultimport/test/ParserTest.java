package com.sap.sailing.odf.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.odf.resultimport.Athlete.Gender;
import com.sap.sailing.odf.resultimport.Competition;
import com.sap.sailing.odf.resultimport.CumulativeResult;
import com.sap.sailing.odf.resultimport.CumulativeResultDocumentProvider;
import com.sap.sailing.odf.resultimport.OdfBody;
import com.sap.sailing.odf.resultimport.OdfBodyParser;
import com.sap.sailing.odf.resultimport.ParserFactory;
import com.sap.sailing.odf.resultimport.Skipper;
import com.sap.sailing.odf.resultimport.impl.ScoreCorrectionProviderImpl;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME = "SAM004000_DT_CUMULATIVE_RESULT_SAM004000_1.0.xml";
    private static final String SAMPLE_INPUT_NAME2 = "SAM007000_DT_CUMULATIVE_RESULT_SAM007000_1.0.xml";
    private static final String RESOURCES = "resources/";

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }
    
    private CumulativeResultDocumentProvider getTestDocumentProvider() {
        return new CumulativeResultDocumentProvider() {
            @Override
            public Iterable<InputStream> getAllAvailableCumulativeResultDocuments() {
                try {
                    return Arrays.asList(new InputStream[] { getInputStream(SAMPLE_INPUT_NAME), getInputStream(SAMPLE_INPUT_NAME2) });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
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
        final Map<String, CumulativeResult> results = new HashMap<>();
        for (Competition competition : competitions) {
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                System.out.println(cumulativeResult);
                results.put(cumulativeResult.getCompetitorCode(), cumulativeResult);
            }
        }
        assertTrue(results.containsKey("1094919"));
        CumulativeResult resultsFor1094919 = results.get("1094919");
        assertEquals(1, Util.size(resultsFor1094919.getAthletes()));
        assertEquals("SLINGSBY Tom", resultsFor1094919.getAthletes().iterator().next().getName());
        assertEquals(Gender.M, resultsFor1094919.getAthletes().iterator().next().getGender());
        assertTrue(resultsFor1094919.getAthletes().iterator().next() instanceof Skipper);
        assertTrue(results.containsKey("262797"));
        CumulativeResult resultsFor262797 = results.get("262797");
        assertEquals(1, Util.size(resultsFor262797.getAthletes()));
        assertEquals("IGNATEV Ilia", resultsFor262797.getAthletes().iterator().next().getName());
        assertEquals(Gender.M, resultsFor262797.getAthletes().iterator().next().getGender());
    }
    
    @Test
    public void testScoreCorrectionProviderFeeding() throws IOException, SAXException, ParserConfigurationException {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                ParserFactory.INSTANCE);
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        assertTrue(hasResultsFor.containsKey("Star"));
        assertTrue(hasResultsFor.containsKey("Laser"));
    }
    
}
