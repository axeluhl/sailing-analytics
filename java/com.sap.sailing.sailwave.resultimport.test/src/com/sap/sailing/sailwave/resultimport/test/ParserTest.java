package com.sap.sailing.sailwave.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sailing.sailwave.resultimport.CsvParserFactory;
import com.sap.sailing.sailwave.resultimport.impl.ScoreCorrectionProviderImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME_49er = "49er_R1-6.csv";
    private static final String SAMPLE_INPUT_NAME_49erFX = "49erFX_R1-6.csv";
    private static final String SAMPLE_INPUT_NAME_505 = "505_SouthAfrica.csv";
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
                    List<ResultDocumentDescriptor> result = new ArrayList<>();
                    TimePoint now = MillisecondsTimePoint.now();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_49er), SAMPLE_INPUT_NAME_49er, now));
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_49erFX), SAMPLE_INPUT_NAME_49erFX, now));
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(SAMPLE_INPUT_NAME_505), SAMPLE_INPUT_NAME_505, now));
                    return result;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void testParsingSomeDocuments() throws Exception {
        RegattaResults parseResults49er = CsvParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_49er),
                SAMPLE_INPUT_NAME_49er, MillisecondsTimePoint.now()).parseResults();
        assertNotNull(parseResults49er);

        RegattaResults parseResults49erFX = CsvParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_49erFX),
                SAMPLE_INPUT_NAME_49erFX, MillisecondsTimePoint.now()).parseResults();
        assertNotNull(parseResults49erFX);

        RegattaResults parseResults505 = CsvParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_505),
                SAMPLE_INPUT_NAME_505, MillisecondsTimePoint.now()).parseResults();
        assertNotNull(parseResults505);
    }

    @Test
    public void testScoreCorrectionProvider49er() throws Exception {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                CsvParserFactory.INSTANCE);
        Map<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        RegattaScoreCorrections result49er = scoreCorrectionProvider.getScoreCorrections(SAMPLE_INPUT_NAME_49er,
                "49er", hasResultsFor.get(SAMPLE_INPUT_NAME_49er).iterator().next().getB());
        assertNotNull(result49er);
        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = result49er.getScoreCorrectionsForRaces();
        assertNotNull(scoreCorrectionsForRaces);
        assertEquals(6, Util.size(scoreCorrectionsForRaces)); // 6 races
        {
            final ScoreCorrectionsForRace resultsForR2 = Util.get(scoreCorrectionsForRaces, 1);
            assertEquals(32, resultsForR2.getScoreCorrectionForCompetitor("SWE 116").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.BFD, resultsForR2.getScoreCorrectionForCompetitor("SWE 116").getMaxPointsReason());
            assertEquals("Fritiof Hedström+Jonatan Bergström",
                    resultsForR2.getScoreCorrectionForCompetitor("SWE 116").getCompetitorName());
        }
    }
    
    @Test
    public void testScoreCorrectionProvider505() throws Exception {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                CsvParserFactory.INSTANCE);
        Map<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        RegattaScoreCorrections result505 = scoreCorrectionProvider.getScoreCorrections(SAMPLE_INPUT_NAME_505,
                "505", hasResultsFor.get(SAMPLE_INPUT_NAME_505).iterator().next().getB());
        assertNotNull(result505);
        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = result505.getScoreCorrectionsForRaces();
        assertNotNull(scoreCorrectionsForRaces);
        assertEquals(5, Util.size(scoreCorrectionsForRaces)); // 5 races with values out of 9 races
        
        {
            final ScoreCorrectionsForRace resultsForR2 = Util.get(scoreCorrectionsForRaces, 1);
            assertEquals(3, resultsForR2.getScoreCorrectionForCompetitor("RSA 8719").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.DNC, resultsForR2.getScoreCorrectionForCompetitor("GER 9026").getMaxPointsReason());
            assertEquals("Tina Plattner+Holger Yess",
                    resultsForR2.getScoreCorrectionForCompetitor("GER 9026").getCompetitorName());
        }
    }
}

