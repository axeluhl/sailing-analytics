package com.sap.sailing.freg.resultimport.test;

import static org.junit.Assert.assertEquals;
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
            List<String> tdContent = parser.getTdContents(rowContents.get(i));
            assertEquals("Expected "+tdContent+" index "+i+" to have size 14 but was "+tdContent.size(), 14, tdContent.size());
        }
    }
}
