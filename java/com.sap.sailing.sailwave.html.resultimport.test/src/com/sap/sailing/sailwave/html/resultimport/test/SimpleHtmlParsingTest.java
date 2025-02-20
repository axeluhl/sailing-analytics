package com.sap.sailing.sailwave.html.resultimport.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.resultimport.CompetitorRow;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.sailwave.html.resultimport.impl.SailwaveHtmlParser;
import com.sap.sse.common.Util;

public class SimpleHtmlParsingTest {
    @Test
    public void testOpenSampleHtml() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("ILCA4.html")));
        final String readLine = br.readLine();
        assertNotNull(readLine);
        br.close();
    }

    @Test
    public void testMultiLineTD() throws IOException {
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("ILCA6.html");
        SailwaveHtmlParser parser = new SailwaveHtmlParser();
        final RegattaResults regattaResults = parser.getRegattaResults(resourceAsStream);
        List<CompetitorRow> result = regattaResults.getCompetitorResults();
        assertFalse(result.isEmpty());
        assertEquals(17, result.size());
        assertTrue(regattaResults.getMetadata().get("seriestitle").contains("Results are provisional as of  9:43 on February 16, 2025"));
        for (CompetitorRow row : result) {
            if (Util.contains(row.getNames(), "Daniel Kin Chung Chan")) {
                assertEquals("HKG 223750", row.getSailID());
                assertEquals(25.0, row.getScoreAfterDiscarding(), 0.000000001);
                assertEquals(40.0, row.getTotalPointsBeforeDiscarding(), 0.000000001);
            }
        }
    }
    
    @Test
    public void testEmptyAndDashedScores() throws IOException {
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("ILCA7.html");
        SailwaveHtmlParser parser = new SailwaveHtmlParser();
        List<CompetitorRow> result = parser.getRegattaResults(resourceAsStream).getCompetitorResults();
        assertFalse(result.isEmpty());
        assertEquals(117, result.size());
        for (CompetitorRow row : result) {
            if (Util.contains(row.getNames(), "Nicholas Halliday")) {
                assertEquals("HKG 171979", row.getSailID());
                assertEquals(7.0, row.getScoreAfterDiscarding(), 0.000000001);
                assertEquals(10.0, row.getTotalPointsBeforeDiscarding(), 0.000000001);
                assertNull(Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getRank());
                assertNull(Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getScore());
                assertEquals(false, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).isDiscarded());
                assertNull(Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).getRank());
                assertEquals((double) 118.00, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).getScore(), 0.000000001);
                assertEquals(false, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).isDiscarded());
            } else if (Util.contains(row.getNames(), "HAGAN Douglas")) {
                assertEquals("AUS 8817", row.getSailID());
                assertEquals(235.00, row.getScoreAfterDiscarding(), 0.000000001);
                assertEquals(235.00, row.getTotalPointsBeforeDiscarding(), 0.000000001);
                assertNull(Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getRank());
                assertEquals("DNF", Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getMaxPointsReason());
                assertEquals((double) 117.00, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getScore(), 0.000000001);
                assertEquals(false, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).isDiscarded());
                assertNull(Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).getRank());
                assertEquals("DNF", Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).getMaxPointsReason());
                assertEquals((double) 118.00, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).getScore(), 0.000000001);
                assertEquals(false, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).isDiscarded());
            }
        }
    }
    
    @Test
    public void testGetCompetitorRows505() throws IOException {
        SailwaveHtmlParser parser = new SailwaveHtmlParser();
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("29er.html");
        List<CompetitorRow> result = parser.getRegattaResults(resourceAsStream).getCompetitorResults();
        assertFalse(result.isEmpty());
        assertEquals(75, result.size());
        for (CompetitorRow row : result) {
            if (Util.contains(row.getNames(), "HOLZAPFEL Alexander")) {
                assertEquals("GER 8975", row.getSailID());
                assertEquals(60.00, row.getScoreAfterDiscarding(), 0.000000001);
                assertEquals(74.00, row.getTotalPointsBeforeDiscarding(), 0.000000001);
                assertEquals((int) 11, (int) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getRank());
                assertEquals((double) 11, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getScore(), 0.000000001);
                assertEquals(false, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).isDiscarded());
            } else if (Util.contains(row.getNames(), "BART Cedric")) {
                assertEquals("SUI 8543", row.getSailID());
                assertEquals(55.00, row.getScoreAfterDiscarding(), 0.000000001);
                assertEquals(74.00, row.getTotalPointsBeforeDiscarding(), 0.000000001);
                assertEquals((int) 2, (int) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 2).getRank());
                assertEquals((double) 2, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 2).getScore(), 0.000000001);
                assertEquals(false, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 2).isDiscarded());
                assertEquals((int) 19, (int) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 3).getRank());
                assertEquals((double) 19, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 3).getScore(), 0.000000001);
                assertEquals(true, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 3).isDiscarded());
            } else if (Util.contains(row.getNames(), "LEWNS Chris")) {
                assertEquals("GBR 9057", row.getSailID());
                assertEquals(64.00, row.getScoreAfterDiscarding(), 0.000000001);
                assertEquals(140.00, row.getTotalPointsBeforeDiscarding(), 0.000000001);
                assertNull(Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 7).getRank());
                assertEquals("DNF", Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 7).getMaxPointsReason());
                assertEquals((double) 76, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 7).getScore(), 0.000000001);
                assertEquals(true, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 7).isDiscarded());
            }
        }
    }

    @Test
    public void testGetCompetitorRows29er() throws IOException {
        SailwaveHtmlParser parser = new SailwaveHtmlParser();
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("29er.html");
        List<CompetitorRow> result = parser.getRegattaResults(resourceAsStream).getCompetitorResults();
        assertFalse(result.isEmpty());
        assertEquals(62, result.size());
        for (CompetitorRow row : result) {
            if (Util.contains(row.getNames(), "HOLSTE Kim (M1993)")) {
                assertEquals("GER 1864", row.getSailID());
                assertEquals(58.00, row.getScoreAfterDiscarding(), 0.000000001);
                assertEquals(169.00, row.getTotalPointsBeforeDiscarding(), 0.000000001);
                assertEquals((int) 25, (int) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getRank());
                assertEquals((double) 25, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).getScore(), 0.000000001);
                assertEquals(false, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 0).isDiscarded());
                assertEquals((int) 38, (int) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).getRank());
                assertEquals((double) 38, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).getScore(), 0.000000001);
                assertEquals(true, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 1).isDiscarded());
                assertNull(Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 6).getRank());
                assertEquals("DSQ", Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 6).getMaxPointsReason());
                assertEquals((double) 63, (double) Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 6).getScore(), 0.000000001);
                assertEquals(true, Util.get(row.getRankAndMaxPointsReasonAndPointsAndDiscarded(), 6).isDiscarded());
            }
        }
    }
}
