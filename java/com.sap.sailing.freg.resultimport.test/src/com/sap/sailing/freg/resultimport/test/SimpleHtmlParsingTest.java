package com.sap.sailing.freg.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

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
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("freg_html_export_sample.html")));
        Pattern tr = Pattern.compile("<(tr|TR)( [^>]*)?>");
        Pattern slashTr = Pattern.compile("</(tr|TR)( [^>]*)?>");
        boolean inTr = false;
        StringBuilder trContents = new StringBuilder();
        List<String> tableRows = new ArrayList<String>(); // the contents of the <tr> tags found
        String readLine = br.readLine();
        int start = 0;
        int end = 0;
        while (readLine != null) {
            if (!inTr) {
                final Matcher trMatcher = tr.matcher(readLine);
                boolean foundTr = trMatcher.find(end);
                if (foundTr) {
                    start = trMatcher.end(0);
                    inTr = true;
                } else {
                    readLine = br.readLine();
                    end = 0;
                }
            } else { // we are within the contents of a <tr> tag here
                final Matcher slashTrMatcher = slashTr.matcher(readLine);
                boolean foundSlashTr = slashTrMatcher.find(start);
                if (foundSlashTr) {
                    end = slashTrMatcher.start();
                    inTr = false;
                    trContents.append(readLine.substring(start, end));
                    tableRows.add(trContents.toString());
                    trContents.delete(0, trContents.length());
                } else {
                    end = readLine.length();
                }
                if (!foundSlashTr) {
                    trContents.append('\n'); 
                    readLine = br.readLine();
                    start = 0;
                }
            }
        }
        br.close();
    }
}
