package com.sap.sailing.manage2sail.resultimport.test;

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
import com.sap.sailing.manage2sail.resultimport.ScoreCorrectionProviderImpl;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sailing.resultimport.impl.ResultUrlRegistryImpl;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME_29er = "YES_29er_XRR.xml";
    private static final String SAMPLE_INPUT_NAME_420 = "YES_420_XRR.xml";
    private static final String SAMPLE_INPUT_NAME_470 = "YES_470_XRR.xml";
    private static final String YES_EVENT_NAME = "YES - Young Europeans Sailing 2013";
    private static final String ISAF_ID_29ER = "29ER";
    private static final String ISAF_ID_420 = "420";
    private static final String ISAF_ID_470 = "470";
    
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

                    Date _29erDate = DatatypeConverter.parseDateTime("2013-06-10T07:55:08.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_29er),
                            null, new MillisecondsTimePoint(_29erDate), YES_EVENT_NAME , null, ISAF_ID_29ER));

                    Date _420Date = DatatypeConverter.parseDateTime("2013-06-10T07:55:08.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_420),
                            null, new MillisecondsTimePoint(_420Date), YES_EVENT_NAME , null, ISAF_ID_420));

                    Date _470Date = DatatypeConverter.parseDateTime("2013-06-10T07:55:08.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_470),
                            null, new MillisecondsTimePoint(_470Date), YES_EVENT_NAME , null, ISAF_ID_470));
                    
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void testSimpleParsingSomeManage2SailDocuments() throws JAXBException, IOException {
        RegattaResults r1 = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_29er), YES_EVENT_NAME).parse();
        assertNotNull(r1);

        RegattaResults r2 = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_420), YES_EVENT_NAME).parse();
        assertNotNull(r2);

        RegattaResults r3 = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_470), YES_EVENT_NAME).parse();
        assertNotNull(r3);
    }

    @Test
    public void testScoreCorrectionProviderFeedingAndHasResults() throws IOException, SAXException,
            ParserConfigurationException, JAXBException {
        ResultUrlRegistry resultUrlRegistry = new ResultUrlRegistryImpl(mock(MongoObjectFactory.class),
                mock(DomainObjectFactory.class));
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(
                getTestDocumentProvider(), ParserFactory.INSTANCE, resultUrlRegistry);
        Map<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        
        Set<com.sap.sse.common.Util.Pair<String, TimePoint>> resultsForYES = hasResultsFor.get(YES_EVENT_NAME);
        assertNotNull(resultsForYES);

        assertEquals(3, resultsForYES.size());
    }
    
    @Test
    public void testScoreCorrectionProvider() throws Exception {
        ResultUrlRegistry resultUrlRegistry = new ResultUrlRegistryImpl(mock(MongoObjectFactory.class),
                mock(DomainObjectFactory.class));
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(
                getTestDocumentProvider(), ParserFactory.INSTANCE, resultUrlRegistry);
        Map<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        Set<com.sap.sse.common.Util.Pair<String, TimePoint>> resultsForYES = hasResultsFor.get(YES_EVENT_NAME);
        com.sap.sse.common.Util.Pair<String, TimePoint> resultFor29er = null;
        for(com.sap.sse.common.Util.Pair<String, TimePoint> result: resultsForYES) {
            if(result.getA().equals(ISAF_ID_29ER)) {
                resultFor29er = result;
                break;
            }
        }
        assertNotNull(resultFor29er);
        
        RegattaScoreCorrections _29erResult = scoreCorrectionProvider.getScoreCorrections(YES_EVENT_NAME, ISAF_ID_29ER , resultFor29er.getB());
        assertNotNull(_29erResult);
        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = _29erResult.getScoreCorrectionsForRaces();
        assertNotNull(scoreCorrectionsForRaces);
        assertEquals(8, Util.size(scoreCorrectionsForRaces)); 
    }

}
