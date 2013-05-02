package com.sap.sailing.velum.resultimport.test;

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

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.velum.resultimport.CsvParserFactory;
import com.sap.sailing.velum.resultimport.impl.ScoreCorrectionProviderImpl;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME = "HVR2012.csv";
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
            public Iterable<Triple<InputStream, String, TimePoint>> getDocumentsAndNamesAndLastModified() throws FileNotFoundException {
                try {
                    List<Triple<InputStream, String, TimePoint>> result = new ArrayList<>();
                    TimePoint now = MillisecondsTimePoint.now();
                    result.add(new Triple<InputStream, String, TimePoint>(getInputStream(SAMPLE_INPUT_NAME), SAMPLE_INPUT_NAME, now));
                    return result;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void testSimpleParsingSomeSampleDocument() throws Exception {
        RegattaResults parseResults = CsvParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME), SAMPLE_INPUT_NAME, MillisecondsTimePoint.now()).parseResults();
        assertNotNull(parseResults);
    }

    @Test
    public void testScoreCorrectionProvider() throws Exception {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                CsvParserFactory.INSTANCE);
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        RegattaScoreCorrections starResult = scoreCorrectionProvider.getScoreCorrections("Star", "Star", hasResultsFor.get("Star").iterator().next().getB());
        assertNotNull(starResult);
        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = starResult.getScoreCorrectionsForRaces();
        assertNotNull(scoreCorrectionsForRaces);
        assertEquals(7, Util.size(scoreCorrectionsForRaces)); // 7 races
        {
            final ScoreCorrectionsForRace resultsForR3 = Util.get(scoreCorrectionsForRaces, 2);
            assertEquals(38, resultsForR3.getScoreCorrectionForCompetitor("GER1009").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.OCS, resultsForR3.getScoreCorrectionForCompetitor("GER1009").getMaxPointsReason());
            assertEquals("Matthiesen, Ulrich+Imbeck, Torsten+Imbeck, Anton",
                    resultsForR3.getScoreCorrectionForCompetitor("GER1009").getCompetitorName());
        }
        {
            final ScoreCorrectionsForRace resultsForR2 = Util.get(scoreCorrectionsForRaces, 1);
            assertEquals(10, resultsForR2.getScoreCorrectionForCompetitor("GER938").getPoints(), 0.00000001);
            assertSame(null, resultsForR2.getScoreCorrectionForCompetitor("GER938").getMaxPointsReason());
        }
    }
}
