package com.sap.sailing.freg.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.sap.sailing.freg.resultimport.CompetitorRow;
import com.sap.sailing.freg.resultimport.impl.FregHtmlParser;

public class SimpleHtmlParsingTest {
    @Test
    public void testOpenSampleHtml() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("freg_html_export_sample.html")));
        final String readLine = br.readLine();
        assertNotNull(readLine);
        br.close();
    }
    
    @Test
    public void matchAngleBracketTest() {
        Pattern tr = Pattern.compile("<(tr|TR)( [^>]*)?>");
        Matcher matcher = tr.matcher("Humba humba <tr><td>Trala</td></td>");
        assertTrue(matcher.find(0));
        assertEquals("<tr>", matcher.group());
        Matcher matcher2 = tr.matcher("Humba humba <tr color=#123456><td>Trala</td></td>");
        assertTrue(matcher2.find(0));
        assertEquals("<tr color=#123456>", matcher2.group());
    }
    
    @Test
    public void testFindTableRows() throws IOException {
        FregHtmlParser parser = new FregHtmlParser();
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("freg_html_export_sample.html");
        List<String> rowContents = parser.getRowContents(resourceAsStream);
        assertNotNull(rowContents);
        assertTrue(!rowContents.isEmpty());
        resourceAsStream.close();
        for (int i=2; i<rowContents.size()-1; i++) {
            List<String> tdContent = parser.getTagContents(rowContents.get(i), "td");
            assertEquals("Expected "+tdContent+" index "+i+" to have size 14 but was "+tdContent.size(), 14, tdContent.size());
        }
    }
    
    @Test
    public void testGetCompetitorRows() throws IOException {
        FregHtmlParser parser = new FregHtmlParser();
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("freg_html_export_sample.html");
        List<CompetitorRow> result = parser.getCompetitorRows(resourceAsStream);
        assertFalse(result.isEmpty());
        assertEquals(75, result.size());
        for (CompetitorRow row : result) {
            if (row.getNames().contains("HOLZAPFEL Alexander")) {
                assertEquals("GER 8975", row.getSailID());
                assertEquals(60.00, row.getScoreAfterDiscarding(), 0.000000001);
                assertEquals(74.00, row.getTotalPointsBeforeDiscarding(), 0.000000001);
                assertEquals((int) 11, (int) row.getRankAndMaxPointsReasonAndPointsAndDiscarded().get(0).getRank());
                assertEquals((double) 11, (double) row.getRankAndMaxPointsReasonAndPointsAndDiscarded().get(0).getScore(), 0.000000001);
                assertEquals(false, row.getRankAndMaxPointsReasonAndPointsAndDiscarded().get(0).isDiscarded());
            }
        }
    }
}
