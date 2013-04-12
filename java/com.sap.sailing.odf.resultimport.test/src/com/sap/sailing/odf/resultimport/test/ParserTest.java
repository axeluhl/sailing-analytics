package com.sap.sailing.odf.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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

import com.sap.sailing.domain.common.CountryCodeFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
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
import com.sap.sailing.odf.resultimport.PointsResult;
import com.sap.sailing.odf.resultimport.Skipper;
import com.sap.sailing.odf.resultimport.impl.ScoreCorrectionProviderImpl;

public class ParserTest {
    private static final String SAMPLE_INPUT_NAME_LASER = "SAM004000_DT_CUMULATIVE_RESULT_SAM004000_1.0.xml";
    private static final String SAMPLE_INPUT_NAME_STAR = "SAM007000_DT_CUMULATIVE_RESULT_SAM007000_1.0.xml";
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
                    return Arrays.asList(new InputStream[] { getInputStream(SAMPLE_INPUT_NAME_LASER), getInputStream(SAMPLE_INPUT_NAME_STAR) });
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
        return getInputStream(SAMPLE_INPUT_NAME_LASER);
    }
    
    @Test
    public void testParserForLaserRegatta() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        OdfBodyParser parser = ParserFactory.INSTANCE.createOdfBodyParser();
        OdfBody body = parser.parse(getInputStream(SAMPLE_INPUT_NAME_LASER), SAMPLE_INPUT_NAME_LASER);
        assertNotNull(body);
        Iterable<Competition> competitions = body.getCompetitions();
        final Map<String, CumulativeResult> results = new HashMap<>();
        for (Competition competition : competitions) {
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                results.put(cumulativeResult.getCompetitorCode(), cumulativeResult);
            }
        }
        {
            assertTrue(results.containsKey("1094919"));
            CumulativeResult resultsFor1094919 = results.get("1094919");
            assertEquals(1, Util.size(resultsFor1094919.getAthletes()));
            assertEquals("SLINGSBY Tom", resultsFor1094919.getAthletes().iterator().next().getName());
            assertEquals(Gender.M, resultsFor1094919.getAthletes().iterator().next().getGender());
            assertTrue(resultsFor1094919.getAthletes().iterator().next() instanceof Skipper);
            assertEquals(18, resultsFor1094919.getPointsInMedalRace(), 0.00000001);
            assertSame(CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName("AUS"), resultsFor1094919.getCountryCode());
            assertEquals(57, resultsFor1094919.getTotalPoints(), 0.00000001);
            assertEquals(43, ((PointsResult) resultsFor1094919.getResult()).getPoints(), 0.00000001);
        }
        {
            assertTrue(results.containsKey("262797"));
            CumulativeResult resultsFor262797 = results.get("262797");
            assertEquals(1, Util.size(resultsFor262797.getAthletes()));
            assertEquals("IGNATEV Ilia", resultsFor262797.getAthletes().iterator().next().getName());
            assertEquals(Gender.M, resultsFor262797.getAthletes().iterator().next().getGender());
            assertNull(resultsFor262797.getPointsInMedalRace());
            assertSame(CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName("KGZ"), resultsFor262797.getCountryCode());
            assertEquals(471, resultsFor262797.getTotalPoints(), 0.00000001);
            assertEquals(422, ((PointsResult) resultsFor262797.getResult()).getPoints(), 0.00000001);
        }
        {
            assertTrue(results.containsKey("1060133"));
            CumulativeResult resultsFor1060133 = results.get("1060133");
            assertSame(MaxPointsReason.DSQ, Util.get(resultsFor1060133.getPointsAndRanksAfterEachRace(), 6).getC());
            assertEquals(50, Util.get(resultsFor1060133.getPointsAndRanksAfterEachRace(), 6).getA(), 0.0000001);
            assertEquals(Integer.valueOf(48), Util.get(resultsFor1060133.getPointsAndRanksAfterEachRace(), 6).getB());
        }
    }
    
    @Test
    public void testParserForStarRegatta() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        OdfBodyParser parser = ParserFactory.INSTANCE.createOdfBodyParser();
        OdfBody body = parser.parse(getInputStream(SAMPLE_INPUT_NAME_STAR), SAMPLE_INPUT_NAME_STAR);
        assertNotNull(body);
        Iterable<Competition> competitions = body.getCompetitions();
        final Map<String, CumulativeResult> results = new HashMap<>();
        for (Competition competition : competitions) {
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                results.put(cumulativeResult.getCompetitorCode(), cumulativeResult);
            }
        }
        {
            assertTrue(results.containsKey("SAM007SWE01"));
            CumulativeResult resultsForSAM007SWE01 = results.get("SAM007SWE01");
            assertEquals(2, Util.size(resultsForSAM007SWE01.getAthletes()));
            assertEquals("LOOF Fredrik", resultsForSAM007SWE01.getAthletes().iterator().next().getName());
            assertEquals(Gender.M, resultsForSAM007SWE01.getAthletes().iterator().next().getGender());
            assertTrue(resultsForSAM007SWE01.getAthletes().iterator().next() instanceof Skipper);
            assertEquals(2, resultsForSAM007SWE01.getPointsInMedalRace(), 0.00000001);
            assertSame(CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName("SWE"), resultsForSAM007SWE01.getCountryCode());
            assertEquals(42, resultsForSAM007SWE01.getTotalPoints(), 0.00000001);
            assertEquals(32, ((PointsResult) resultsForSAM007SWE01.getResult()).getPoints(), 0.00000001);
        }
        {
            assertTrue(results.containsKey("SAM007CRO01"));
            CumulativeResult resultsForSAM007CRO01 = results.get("SAM007CRO01");
            assertEquals(2, Util.size(resultsForSAM007CRO01.getAthletes()));
            assertEquals("LOVROVIC Marin", resultsForSAM007CRO01.getAthletes().iterator().next().getName());
            assertEquals(Gender.M, resultsForSAM007CRO01.getAthletes().iterator().next().getGender());
            assertNull(resultsForSAM007CRO01.getPointsInMedalRace());
            assertSame(CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName("CRO"), resultsForSAM007CRO01.getCountryCode());
            assertEquals(132, resultsForSAM007CRO01.getTotalPoints(), 0.00000001);
            assertEquals(116, ((PointsResult) resultsForSAM007CRO01.getResult()).getPoints(), 0.00000001);
        }
        {
            assertTrue(results.containsKey("SAM007GRE01"));
            CumulativeResult resultsFor1060133 = results.get("SAM007GRE01");
            assertSame(MaxPointsReason.OCS, Util.get(resultsFor1060133.getPointsAndRanksAfterEachRace(), 8).getC());
            assertEquals(17, Util.get(resultsFor1060133.getPointsAndRanksAfterEachRace(), 8).getA(), 0.0000001);
            assertEquals(Integer.valueOf(14), Util.get(resultsFor1060133.getPointsAndRanksAfterEachRace(), 8).getB());
        }
    }
    
    @Test
    public void testScoreCorrectionProviderFeedingAndHasResults() throws IOException, SAXException,
            ParserConfigurationException {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                ParserFactory.INSTANCE);
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        assertTrue(hasResultsFor.containsKey("Star"));
        assertTrue(hasResultsFor.containsKey("Laser"));
    }
    
    @Test
    public void testScoreCorrectionProvider() throws IOException, SAXException,
            ParserConfigurationException {
        ScoreCorrectionProviderImpl scoreCorrectionProvider = new ScoreCorrectionProviderImpl(getTestDocumentProvider(),
                ParserFactory.INSTANCE);
        Map<String, Set<Pair<String, TimePoint>>> hasResultsFor = scoreCorrectionProvider.getHasResultsForBoatClassFromDateByEventName();
        RegattaScoreCorrections starResult = scoreCorrectionProvider.getScoreCorrections("SAM007000@WAP", "Star", hasResultsFor.get("Star").iterator().next().getB());
        assertNotNull(starResult);
        Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces = starResult.getScoreCorrectionsForRaces();
        assertNotNull(scoreCorrectionsForRaces);
        assertEquals(11, Util.size(scoreCorrectionsForRaces)); // 10 regular races, one medal race
        {
            final ScoreCorrectionsForRace resultsForR6 = Util.get(scoreCorrectionsForRaces, 5);
            assertEquals(11, resultsForR6.getScoreCorrectionForCompetitor("SAM007NOR01").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.NONE, resultsForR6.getScoreCorrectionForCompetitor("SAM007NOR01")
                    .getMaxPointsReason());
            assertEquals("MELLEBY Eivind + PEDERSEN Petter Morland",
                    resultsForR6.getScoreCorrectionForCompetitor("SAM007NOR01").getCompetitorName());
        }
        {
            final ScoreCorrectionsForRace resultsForM1 = Util.get(scoreCorrectionsForRaces, 10);
            assertEquals(10, resultsForM1.getScoreCorrectionForCompetitor("SAM007NOR01").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.NONE, resultsForM1.getScoreCorrectionForCompetitor("SAM007NOR01")
                    .getMaxPointsReason());
            assertEquals("MELLEBY Eivind + PEDERSEN Petter Morland",
                    resultsForM1.getScoreCorrectionForCompetitor("SAM007NOR01").getCompetitorName());
        }
        {
            final ScoreCorrectionsForRace resultsForR7 = Util.get(scoreCorrectionsForRaces, 6);
            assertEquals(17, resultsForR7.getScoreCorrectionForCompetitor("SAM007IRL01").getPoints(), 0.00000001);
            assertSame(MaxPointsReason.DSQ, resultsForR7.getScoreCorrectionForCompetitor("SAM007IRL01")
                    .getMaxPointsReason());
            assertEquals("O'LEARY Peter + BURROWS David",
                    resultsForR7.getScoreCorrectionForCompetitor("SAM007IRL01").getCompetitorName());
        }
    }
}
