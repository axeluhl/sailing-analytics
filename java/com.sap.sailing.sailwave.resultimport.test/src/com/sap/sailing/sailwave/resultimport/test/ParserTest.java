package com.sap.sailing.sailwave.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sailing.sailwave.resultimport.ScoreCorrectionProviderImpl;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME_49er = "EYSEC 2012 - TEST ISAF XRR.xml";
    private static final String SAMPLE_INPUT_NAME_49er_FX = "49erFX ISAf XRR.xml";
    
    private static final String EUROSAF_YOUTH_EVENT_NAME = "2012 Eurosaf Youth Sailing European Championship";
    private static final String _49FX_EUROPEANS_EVENT_NAME = "49erFX European Championship";
    
    private static final String ISAF_ID_49ER = "49ER";
    private static final String ISAF_ID_49FX = "49ERFX";
    
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

                    Date _49erDate = DatatypeConverter.parseDateTime("2013-06-10T07:55:08.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_49er),
                            null, new MillisecondsTimePoint(_49erDate), EUROSAF_YOUTH_EVENT_NAME , null, ISAF_ID_49ER));

                    Date _49erFXDate = DatatypeConverter.parseDateTime("2013-07-02T20:04:55.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_49er_FX),
                            null, new MillisecondsTimePoint(_49erFXDate), _49FX_EUROPEANS_EVENT_NAME , null, ISAF_ID_49FX));
                    
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void testSimpleParsingSomeSailwaveDocuments() throws JAXBException, IOException {
        RegattaResults r1 = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_49er), EUROSAF_YOUTH_EVENT_NAME).parse();
        assertNotNull(r1);

        RegattaResults r2 = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_49er_FX), _49FX_EUROPEANS_EVENT_NAME).parse();
        assertNotNull(r2);

        // TimePoint calculateTimePointForRegattaResults = XRRParserUtil.calculateTimePointForRegattaResults(r1);
    }

    @Test
    public void testScoreCorrectionProviderFeedingAndHasResults() throws IOException, SAXException,
            ParserConfigurationException, JAXBException {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                ParserFactory.INSTANCE);
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        
        Set<Pair<String, TimePoint>> resultsForEUROSAF = hasResultsFor.get(EUROSAF_YOUTH_EVENT_NAME);
        assertNotNull(resultsForEUROSAF);

        assertEquals(1, resultsForEUROSAF.size());
        
        Set<Pair<String, TimePoint>> resultsFor49erFXEuropeans = hasResultsFor.get(_49FX_EUROPEANS_EVENT_NAME);
        assertNotNull(resultsFor49erFXEuropeans);

        assertEquals(1, resultsFor49erFXEuropeans.size());
    }
    
    @Test
    public void testScoreCorrectionProvider() throws Exception {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                ParserFactory.INSTANCE);
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        Set<Pair<String, TimePoint>> resultsForYES = hasResultsFor.get(EUROSAF_YOUTH_EVENT_NAME);
        Pair<String, TimePoint> resultFor49er = null;
        for(Pair<String, TimePoint> result: resultsForYES) {
            if(result.getA().equals(ISAF_ID_49ER)) {
                resultFor49er = result;
                break;
            }
        }
        assertNotNull(resultFor49er);
        
        RegattaScoreCorrections _49erResult = scoreCorrectionProvider.getScoreCorrections(EUROSAF_YOUTH_EVENT_NAME, ISAF_ID_49ER , resultFor49er.getB());
        assertNotNull(_49erResult);
        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = _49erResult.getScoreCorrectionsForRaces();
        assertNotNull(scoreCorrectionsForRaces);
        assertEquals(0, Util.size(scoreCorrectionsForRaces)); 
    }

}
