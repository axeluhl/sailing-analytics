package com.sap.sailing.freg.resultimport.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.freg.resultimport.CompetitorRow;

public class FregHtmlParser {
    private static final Logger logger = Logger.getLogger(FregHtmlParser.class.getName());
    
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

    public List<String> getTagContents(String s, String tag) {
        Pattern tagPattern = Pattern.compile("<("+tag.toLowerCase()+"|"+tag.toUpperCase()+")( [^>]*)?>");
        Pattern slashTag = Pattern.compile("</("+tag.toLowerCase()+"|"+tag.toUpperCase()+")( [^>]*)?>");
        boolean inTag = false;
        List<String> result = new ArrayList<String>(); // the contents of the <td> tags found
        int start = 0;
        int end = 0;
        boolean finished = false;
        while (!finished) {
            if (!inTag) {
                final Matcher tagMatcher = tagPattern.matcher(s);
                boolean foundTag = tagMatcher.find(end);
                if (foundTag) {
                    start = tagMatcher.end(0);
                    inTag = true;
                } else {
                    finished = true;
                }
            } else { // we are within the contents of a <tr> tag here
                final Matcher slashTagMatcher = slashTag.matcher(s);
                boolean foundSlashTag = slashTagMatcher.find(start);
                if (foundSlashTag) {
                    end = slashTagMatcher.start();
                    inTag = false;
                    result.add(s.substring(start, end).trim());
                } else {
                    result.add(s.substring(start).trim());
                    finished = true;
                    logger.warning("unclosed "+tag+" tag in string \""+s+"\"");
                }
            }
        }
        return result;
    }
    
    /**
     * @param is closed before the method returns, also in case of exception
     * @throws IOException 
     */
    public List<CompetitorRow> getCompetitorRows(InputStream is) throws IOException {
        try {
            List<CompetitorRow> result = new ArrayList<CompetitorRow>();
            List<String> rowContents = getRowContents(is);
            is.close();
            for (int i = 2; i < rowContents.size() - 1; i++) {
                List<String> tdContent = getTagContents(rowContents.get(i), "td");
                result.add(createCompetitorRow(tdContent));
            }
            return result;
        } finally {
            is.close();
        }
    }

    private CompetitorRow createCompetitorRow(List<String> tdContent) {
        int totalRank = getInt(tdContent.get(0));
        String sailID = getSailID(tdContent.get(1)).replace("&nbsp;", " ");
        // TODO Auto-generated method stub
        return null;
    }

    private String getSailID(String cell) {
        String imgAndSailID = getTagContents(cell, "p").get(0);
        String sailID = getTagContents(imgAndSailID+"</img>", "img").get(0); // closing img may be missing
        return sailID;
    }

    private Integer getInt(String cell) {
        Integer result;
        try {
            result = Integer.valueOf(cell);
        } catch (NumberFormatException nfe) {
            // probably there is a <b> </b> tag combination around the number
            List<String> contents = getTagContents(cell, "b");
            if (!contents.isEmpty()) {
                result = Integer.valueOf(contents.get(0));
            } else {
                result = null;
            }
        }
        return result;
    }
}
