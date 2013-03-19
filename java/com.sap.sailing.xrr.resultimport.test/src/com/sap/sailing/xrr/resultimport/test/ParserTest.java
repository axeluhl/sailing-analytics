package com.sap.sailing.xrr.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.XRRDocumentProvider;
import com.sap.sailing.xrr.resultimport.impl.ScoreCorrectionProviderImpl;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME_LASER = "laser.xml";
    private static final String SAMPLE_INPUT_NAME_STAR = "star.xml";
    private static final String SAMPLE_INPUT_NAME_MELBOURNE = "melbourne_results_actual.xml";
    private static final String RESOURCES = "resources/";

    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }

    private XRRDocumentProvider getTestDocumentProvider() {
        return new XRRDocumentProvider() {
            @Override
            public Iterable<Pair<InputStream, String>> getDocumentsAndNames() throws FileNotFoundException {
                try {
                    List<Pair<InputStream, String>> result = new ArrayList<>();
                    result.add(new Pair<InputStream, String>(getInputStream(SAMPLE_INPUT_NAME_LASER), SAMPLE_INPUT_NAME_LASER));
                    result.add(new Pair<InputStream, String>(getInputStream(SAMPLE_INPUT_NAME_STAR), SAMPLE_INPUT_NAME_STAR));
                    result.add(new Pair<InputStream, String>(getInputStream(SAMPLE_INPUT_NAME_MELBOURNE), SAMPLE_INPUT_NAME_MELBOURNE));
                    return result;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void testSimpleParsingSomeLaserDocument() throws JAXBException, IOException {
        RegattaResults o = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_LASER), SAMPLE_INPUT_NAME_LASER).parse();
        assertNotNull(o);
    }

    @Test
    public void testSimpleParsingSomeStarDocument() throws JAXBException, IOException {
        RegattaResults o = ParserFactory.INSTANCE.createParser(getInputStream(SAMPLE_INPUT_NAME_STAR), SAMPLE_INPUT_NAME_STAR).parse();
        assertNotNull(o);
    }

    @Test
    public void testScoreCorrectionProviderFeedingAndHasResults() throws IOException, SAXException,
            ParserConfigurationException, JAXBException {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                ParserFactory.INSTANCE);
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        assertTrue(hasResultsFor.containsKey("Star"));
        Calendar cal = new GregorianCalendar(2013, /* 2 means March; zero-based */ 2, 15, 18, 51, 15);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        TimePoint expectedTimePoint = new MillisecondsTimePoint(cal.getTime());
        assertEquals(expectedTimePoint, hasResultsFor.get("Star").iterator().next().getB());
        assertTrue(hasResultsFor.containsKey("Laser"));
    }
    
    @Test
    public void testScoreCorrectionProvider() throws IOException, SAXException,
            ParserConfigurationException, JAXBException {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                ParserFactory.INSTANCE);
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        RegattaScoreCorrections starResult = scoreCorrectionProvider.getScoreCorrections("Star Men", "Star", hasResultsFor.get("Star").iterator().next().getB());
        assertNotNull(starResult);
        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = starResult.getScoreCorrectionsForRaces();
        assertNotNull(scoreCorrectionsForRaces);
        assertEquals(11, Util.size(scoreCorrectionsForRaces)); // 10 regular races, one medal race
        {
            final ScoreCorrectionsForRace resultsForR6 = Util.get(scoreCorrectionsForRaces, 5);
            assertEquals(11, resultsForR6.getScoreCorrectionForCompetitor("NOR").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.NONE, resultsForR6.getScoreCorrectionForCompetitor("NOR")
                    .getMaxPointsReason());
            assertEquals("Melleby, Eivind + Pedersen, Petter Morland",
                    resultsForR6.getScoreCorrectionForCompetitor("NOR").getCompetitorName());
        }
        {
            final ScoreCorrectionsForRace resultsForM1 = Util.get(scoreCorrectionsForRaces, 10);
            assertEquals(10, resultsForM1.getScoreCorrectionForCompetitor("NOR").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.NONE, resultsForM1.getScoreCorrectionForCompetitor("NOR")
                    .getMaxPointsReason());
            assertEquals("Melleby, Eivind + Pedersen, Petter Morland",
                    resultsForM1.getScoreCorrectionForCompetitor("NOR").getCompetitorName());
        }
        {
            final ScoreCorrectionsForRace resultsForR7 = Util.get(scoreCorrectionsForRaces, 6);
            assertEquals(17, resultsForR7.getScoreCorrectionForCompetitor("IRL").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.DSQ, resultsForR7.getScoreCorrectionForCompetitor("IRL")
                    .getMaxPointsReason());
            assertEquals("O'leary, Peter + Burrows, David",
                    resultsForR7.getScoreCorrectionForCompetitor("IRL").getCompetitorName());
        }
    }
}
