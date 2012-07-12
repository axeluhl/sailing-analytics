package com.sap.sailing.freg.resultimport.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FregHtmlParser {
    public List<String> getRowContents(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
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
        return tableRows;
    }
}
