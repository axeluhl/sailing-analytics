package com.sap.sailing.yachtscoring.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sailing.resultimport.impl.ResultUrlRegistryImpl;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sailing.yachtscoring.resultimport.ScoreCorrectionProviderImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CharlstonRaceWeek2015_ParserTest {
    private static final String CHARLSTONRACEWEEK2015_TESTFILE_XRR = "event1220_CharlstonRaceWeek2015_xrr.xml";
    private static final String CHARLSTONRACEWEEK2015_EVENT_NAME = "2015 Sperry Charleston Race Week, North Charleston, SC, USA";
    
    private static final String BOAT_CLASS_J111 = "J 111";
    private static final String BOAT_CLASS_MELGES24 = "Melges 24";
    
    private static final String RESOURCES = "resources/";

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    private ResultDocumentProvider getTestDocumentProvider() {
        return new ResultDocumentProvider() {
            @Override
            public Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException {
                try {
                    List<ResultDocumentDescriptor> result = new ArrayList<ResultDocumentDescriptor>();

                    Date _J111Date = DatatypeConverter.parseDateTime("2016-01-19T12:55:08.000Z").getTime();
                    Date _Melges24Date = DatatypeConverter.parseDateTime("2016-01-19T12:55:08.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(CHARLSTONRACEWEEK2015_TESTFILE_XRR),
                            null, new MillisecondsTimePoint(_J111Date), CHARLSTONRACEWEEK2015_EVENT_NAME , null, BOAT_CLASS_J111));
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(CHARLSTONRACEWEEK2015_TESTFILE_XRR),
                            null, new MillisecondsTimePoint(_Melges24Date), CHARLSTONRACEWEEK2015_EVENT_NAME , null, BOAT_CLASS_MELGES24));
                    
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void testSimpleParsingSomeYachtscoringDocuments() throws JAXBException, IOException {
        RegattaResults regattaResults = ParserFactory.INSTANCE.createParser(getInputStream(CHARLSTONRACEWEEK2015_TESTFILE_XRR), CHARLSTONRACEWEEK2015_EVENT_NAME).parse();
        assertNotNull(regattaResults);
    }

    @Test
    public void testScoreCorrectionProviderFeedingAndHasResults() throws IOException, SAXException,
            ParserConfigurationException, JAXBException {
        ResultUrlRegistry resultUrlRegistry = new ResultUrlRegistryImpl(mock(MongoObjectFactory.class),
                mock(DomainObjectFactory.class));
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(
                getTestDocumentProvider(), ParserFactory.INSTANCE, resultUrlRegistry);
        Map<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        
        Set<com.sap.sse.common.Util.Pair<String, TimePoint>> resultsForKeyWestRaceWeek = hasResultsFor.get(CHARLSTONRACEWEEK2015_EVENT_NAME);
        assertNotNull(resultsForKeyWestRaceWeek);

        assertEquals(2, resultsForKeyWestRaceWeek.size());
    }
    
    @Test
    public void testScoreCorrectionProvider() throws Exception {
        ResultUrlRegistry resultUrlRegistry = new ResultUrlRegistryImpl(mock(MongoObjectFactory.class), mock(DomainObjectFactory.class));
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(
                getTestDocumentProvider(), ParserFactory.INSTANCE, resultUrlRegistry);
        Map<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        Set<com.sap.sse.common.Util.Pair<String, TimePoint>> resultsForKeyWestRaceWeek = hasResultsFor.get(CHARLSTONRACEWEEK2015_EVENT_NAME);
        com.sap.sse.common.Util.Pair<String, TimePoint> resultForJ111 = null;
        for(com.sap.sse.common.Util.Pair<String, TimePoint> result: resultsForKeyWestRaceWeek) {
            if(result.getA().equals(BOAT_CLASS_J111)) {
                resultForJ111 = result;
                break;
            }
        }
        assertNotNull(resultForJ111);
        
        RegattaScoreCorrections _J111Result = scoreCorrectionProvider.getScoreCorrections(CHARLSTONRACEWEEK2015_EVENT_NAME, BOAT_CLASS_J111,
                resultForJ111.getB());
        assertNotNull(_J111Result);
        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = _J111Result.getScoreCorrectionsForRaces();
        assertNotNull(scoreCorrectionsForRaces);
        assertEquals(8, Util.size(scoreCorrectionsForRaces)); 
    }

}
