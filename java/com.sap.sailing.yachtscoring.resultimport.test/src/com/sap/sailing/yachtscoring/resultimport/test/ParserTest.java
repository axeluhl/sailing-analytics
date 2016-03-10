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
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME_KEYWESTRACEWEEEK = "event1390_KeyWestRaceWeek2016_xrr.xml";
    private static final String KEYWESTRACEWEEEK_EVENT_NAME = "Key West Race Week 2016";
    private static final String ISAF_ID_29ER = "29ER";
    
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

                    Date _29erDate = DatatypeConverter.parseDateTime("2016-01-19T12:55:08.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_KEYWESTRACEWEEEK),
                            null, new MillisecondsTimePoint(_29erDate), KEYWESTRACEWEEEK_EVENT_NAME , null, ISAF_ID_29ER));
                    
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void testSimpleParsingSomeYachtscoringDocuments() throws JAXBException, IOException {
        RegattaResults r1 = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_KEYWESTRACEWEEEK), KEYWESTRACEWEEEK_EVENT_NAME).parse();
        assertNotNull(r1);

    }

    @Test
    public void testScoreCorrectionProviderFeedingAndHasResults() throws IOException, SAXException,
            ParserConfigurationException, JAXBException {
        ResultUrlRegistry resultUrlRegistry = new ResultUrlRegistryImpl(mock(MongoObjectFactory.class),
                mock(DomainObjectFactory.class));
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(
                getTestDocumentProvider(), ParserFactory.INSTANCE, resultUrlRegistry);
        Map<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        
        Set<com.sap.sse.common.Util.Pair<String, TimePoint>> resultsForYES = hasResultsFor.get(KEYWESTRACEWEEEK_EVENT_NAME);
        assertNotNull(resultsForYES);

        assertEquals(3, resultsForYES.size());
    }
    
//    @Test
//    public void testScoreCorrectionProvider() throws Exception {
//        ResultUrlRegistry resultUrlRegistry = new ResultUrlRegistryImpl(mock(MongoObjectFactory.class),
//                mock(DomainObjectFactory.class));
//        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(
//                getTestDocumentProvider(), ParserFactory.INSTANCE, resultUrlRegistry);
//        Map<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
//        Set<com.sap.sse.common.Util.Pair<String, TimePoint>> resultsForYES = hasResultsFor.get(YES_EVENT_NAME);
//        com.sap.sse.common.Util.Pair<String, TimePoint> resultFor29er = null;
//        for(com.sap.sse.common.Util.Pair<String, TimePoint> result: resultsForYES) {
//            if(result.getA().equals(ISAF_ID_29ER)) {
//                resultFor29er = result;
//                break;
//            }
//        }
//        assertNotNull(resultFor29er);
//        
//        RegattaScoreCorrections _29erResult = scoreCorrectionProvider.getScoreCorrections(YES_EVENT_NAME, ISAF_ID_29ER , resultFor29er.getB());
//        assertNotNull(_29erResult);
//        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = _29erResult.getScoreCorrectionsForRaces();
//        assertNotNull(scoreCorrectionsForRaces);
//        assertEquals(8, Util.size(scoreCorrectionsForRaces)); 
//    }

}
