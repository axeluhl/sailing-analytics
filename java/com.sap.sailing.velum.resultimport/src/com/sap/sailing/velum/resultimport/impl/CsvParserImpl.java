package com.sap.sailing.velum.resultimport.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.resultimport.CompetitorEntry;
import com.sap.sailing.resultimport.CompetitorRow;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.impl.CompetitorRowImpl;
import com.sap.sailing.resultimport.impl.DefaultCompetitorEntryImpl;
import com.sap.sailing.velum.resultimport.CsvParser;
import com.sap.sse.common.TimePoint;

public class CsvParserImpl implements CsvParser {
    private final InputStream inputStream;
    private final String filename;
    private final TimePoint lastModified;

    private final String SEPARATOR = ";"; 
    private static final String STAR_BOAT_CLASS_NAME = "Star";

    public CsvParserImpl(InputStream inputStream, String filename, TimePoint lastModified) {
        this.inputStream = inputStream;
        this.filename = filename;
        this.lastModified = lastModified;
    }

    @Override
    public String getFilename() {
        return filename;
    }
    
    @Override
    public String getBoatClass() {
        String result = STAR_BOAT_CLASS_NAME; 
        // we assume the boat class name is in the filename
        int underScoreIndex = filename.lastIndexOf("_");
        if(underScoreIndex > 0) {
            result = filename.substring(0, underScoreIndex);
        }
        return result;
    }

    @Override
    public RegattaResults parseResults() throws Exception {
        final List<CompetitorRow> competitorRows = new ArrayList<>();

        // Sample line for 7 races
        // G-PL;SEGELNR;STEUERMANN/-FRAU/CREW;CLUB;CLUB-ID;1.Wf;1.Wf;2.Wf;2.Wf;3.Wf;3.Wf;4.Wf;4.Wf;5.Wf;5.Wf;6.Wf;6.Wf;7.Wf;7.Wf;G-PKTE;G-PL;
        // 1;GER    951;Zachariassen, Gerd/Winter Hagen/Zachariassen, Cornelia;NRV/NRV/NRV;HA002/HA002/HA002;1;1,00;3;3,00;4;4,00;8;8,00;[11];[11,00];1;1,00;1;1,00;18,00;1;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "Cp1252"));
        String line = br.readLine();
        int startIndexForRacePoints = -1;
        int endIndexForRacePoints = -1;
        while (line != null) {
            int firstSeparator = line.indexOf(SEPARATOR);
            if (line.startsWith("Wettfahrten")) {
                // ignore line
            } else if (firstSeparator > 0) {
                String firstEntry = line.substring(0, firstSeparator);
                if (firstEntry.startsWith("G-PL") || firstEntry.startsWith("G-Pl")) {
                    // header line
                    // scan the number of races
                    String[] splittedHeadline = line.split(SEPARATOR);
                    for(int i = 0; i < splittedHeadline.length; i++) {
                        if(splittedHeadline != null && splittedHeadline[i].endsWith(".Wf")) {
                            if(startIndexForRacePoints < 0) {
                                startIndexForRacePoints = i;
                            }
                            if(i > endIndexForRacePoints) {
                                endIndexForRacePoints = i;
                            }
                        }
                    }
                } else {
                    try {
                        // check for valid race row
                        Integer.parseInt(firstEntry);
                        
                        String[] splittedRow = line.split(SEPARATOR);
                        String sailID = splittedRow[1];
                        sailID = sailID.replace(" " , "");
                        
                        Integer totalRank = Integer.parseInt(splittedRow[endIndexForRacePoints+2]);
                        Iterable<String> names = Arrays.asList(splittedRow[2].split("/"));
                        List<CompetitorEntry> rankAndMaxPointsReasonAndPointsAndDiscarded = new ArrayList<>();
                        
                        int index = startIndexForRacePoints;
                        while (index <= endIndexForRacePoints) {
                            Integer rank;
                            String maxPointsReason;
                            Double points;
                            boolean discarded = false;
                            
                            String rankOrMaxPointsReason = splittedRow[index++];
                            String pointsAsText = splittedRow[index++];
                            if (rankOrMaxPointsReason.startsWith("[") && rankOrMaxPointsReason.endsWith("]")) {
                                rankOrMaxPointsReason = rankOrMaxPointsReason.substring(1, rankOrMaxPointsReason.length() - 1);
                                discarded = true;
                            }
                            try {
                                rank = Integer.parseInt(rankOrMaxPointsReason);
                                maxPointsReason = null;
                            } catch (NumberFormatException e) {
                                // not a numeric value -> MaxPointsReason
                                rank = null;
                                maxPointsReason = rankOrMaxPointsReason;
                            }
                            if (pointsAsText.startsWith("[") && pointsAsText.endsWith("]")) {
                                pointsAsText = pointsAsText.substring(1, pointsAsText.length() - 1);
                            }
                            pointsAsText = pointsAsText.replaceAll(",",".");
                            points = Double.valueOf(pointsAsText);
                            if (points != 0.0) {
                                CompetitorEntry entry = new DefaultCompetitorEntryImpl(rank,
                                        maxPointsReason, points, discarded);
                                rankAndMaxPointsReasonAndPointsAndDiscarded.add(entry);
                            } else {
                                rankAndMaxPointsReasonAndPointsAndDiscarded.add(null);
                            }
                        }
                        CompetitorRow competitorRow = new CompetitorRowImpl(totalRank, sailID, names, null,
                                null, rankAndMaxPointsReasonAndPointsAndDiscarded);
                        competitorRows.add(competitorRow);
                    } catch (NumberFormatException e) {
                        // no idea
                    }
                }
            } else {
                // probably last line
            }
            line = br.readLine();
        }
        return new RegattaResults() {
            @Override
            public Map<String, String> getMetadata() {
                Map<String, String> result = new HashMap<>();
                return result;
            }
            
            @Override
            public List<CompetitorRow> getCompetitorResults() {
                return competitorRows;
            }
        };
    }

    public TimePoint getLastModified() {
        return lastModified;
    }
}
