package com.sap.sailing.winregatta.resultimport.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.sap.sailing.resultimport.CompetitorEntry;
import com.sap.sailing.resultimport.CompetitorRow;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.impl.CompetitorRowImpl;
import com.sap.sailing.resultimport.impl.DefaultCompetitorEntryImpl;

public class BarbadosResultSpreadsheet {
    private static final int COLUMN_NUMBER_OF_FIRST_RACE_RANK = 5;
    private static final String RACE_SCORE_COLUMN_NAME = "RACE SCORE";
    private static final String OVERALL_RESULTS_SHEET_NAME = "OVERALL RESULTS";
    private Sheet overallResultsSheet;

    public BarbadosResultSpreadsheet(InputStream is) throws Exception {
        Workbook wb = WorkbookFactory.create(is);
        overallResultsSheet = wb.getSheet(OVERALL_RESULTS_SHEET_NAME);
    }
    
    public RegattaResults getRegattaResults() {
        final List<CompetitorRow> competitorRows = new ArrayList<>();
        Row header = overallResultsSheet.getRow(0);
        int i=COLUMN_NUMBER_OF_FIRST_RACE_RANK;
        while (i<=header.getLastCellNum() && !header.getCell(i).getStringCellValue().equals(RACE_SCORE_COLUMN_NAME)) {
            i++;
        }
        if (i>header.getLastCellNum() || !header.getCell(i).getStringCellValue().equals(RACE_SCORE_COLUMN_NAME)) {
            throw new IllegalArgumentException("Didn't find "+RACE_SCORE_COLUMN_NAME+" column");
        }
        final int numberOfRaces = i-COLUMN_NUMBER_OF_FIRST_RACE_RANK;
        final int columnNumberOfNetScore = i;
        final int columnNumberOfTotalScore = columnNumberOfNetScore+1;
        final int columnNumberOfFirstRaceScore = columnNumberOfTotalScore+2;
        int rowIndex = 1;
        Row row;
        String iocCountryCode;
        while ((row=overallResultsSheet.getRow(rowIndex)) != null && (iocCountryCode = row.getCell(1).getStringCellValue()) != null && !iocCountryCode.isEmpty()) {
            Integer totalRank = row.getCell(0) == null ? null : (int) row.getCell(0).getNumericCellValue();
            String sailID = iocCountryCode + ((int) row.getCell(2).getNumericCellValue());
            String helm = row.getCell(3).getStringCellValue();
            String crew = row.getCell(4).getStringCellValue();
            Iterable<String> names = Arrays.asList(new String[] { helm, crew });
            double scoreAfterDiscarding = row.getCell(columnNumberOfTotalScore).getNumericCellValue();
            double netPointsBeforeDiscarding = row.getCell(columnNumberOfNetScore).getNumericCellValue();
            List<CompetitorEntry> rankAndMaxPointsReasonAndPointsAndDiscarded = new ArrayList<>();
            for (int raceNumber=0; raceNumber<numberOfRaces; raceNumber++) {
                int rank = (int) row.getCell(COLUMN_NUMBER_OF_FIRST_RACE_RANK+raceNumber).getNumericCellValue();
                double score = row.getCell(columnNumberOfFirstRaceScore+raceNumber).getNumericCellValue();
                if (score != 0.0) {
                    CompetitorEntry entry = new DefaultCompetitorEntryImpl(rank,
                            /* maxPointsReason */ null, score, /* discarded */ false);
                    rankAndMaxPointsReasonAndPointsAndDiscarded.add(entry);
                } else {
                    rankAndMaxPointsReasonAndPointsAndDiscarded.add(null);
                }
            }
            CompetitorRow competitorRow = new CompetitorRowImpl(totalRank, sailID, names, scoreAfterDiscarding,
                    netPointsBeforeDiscarding, rankAndMaxPointsReasonAndPointsAndDiscarded);
            competitorRows.add(competitorRow);
            rowIndex++;
        }
        return new RegattaResults() {
            @Override
            public Map<String, String> getMetadata() {
                Map<String, String> result = new HashMap<>();
                result.put("boatclassName", "505");
                return result;
            }
            
            @Override
            public List<CompetitorRow> getCompetitorResults() {
                return competitorRows;
            }
        };
    }
}
