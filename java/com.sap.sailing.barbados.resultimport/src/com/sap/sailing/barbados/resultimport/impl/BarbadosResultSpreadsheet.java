package com.sap.sailing.barbados.resultimport.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
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
    private static final String RACE_SCORE_COLUMN_NAME = "TOTAL SCORE";
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
        final int columnNumberOfTotalScore = i;
        final int columnNumberOfNetScore = columnNumberOfTotalScore+1;
        final int columnNumberOfFirstRaceScore = columnNumberOfNetScore+2;
        int rowIndex = 1;
        Row row;
        String iocCountryCode;
        while ((row=overallResultsSheet.getRow(rowIndex)) != null && (iocCountryCode = row.getCell(1).getStringCellValue()) != null && !iocCountryCode.isEmpty()) {
            Integer totalRank = row.getCell(0) == null ? null : (int) row.getCell(0).getNumericCellValue();
            String sailID = iocCountryCode.trim() + " " + ((int) row.getCell(2).getNumericCellValue());
            String helm = row.getCell(3).getStringCellValue();
            String crew = row.getCell(4).getStringCellValue();
            Iterable<String> names = Arrays.asList(new String[] { helm, crew });
            double scoreAfterDiscarding = row.getCell(columnNumberOfNetScore).getNumericCellValue();
            final Cell cellValue = row.getCell(columnNumberOfTotalScore);
            double totalPointsBeforeDiscarding = cellValue == null ? null : cellValue.getNumericCellValue();
            List<CompetitorEntry> rankAndMaxPointsReasonAndPointsAndDiscarded = new ArrayList<>();
            for (int raceNumber=0; raceNumber<numberOfRaces; raceNumber++) {
                final Cell rankOrMaxPointReasonCell = row.getCell(COLUMN_NUMBER_OF_FIRST_RACE_RANK+raceNumber);
                Integer rank;
                String maxPointsReason;
                try {
                    rank = (int) rankOrMaxPointReasonCell.getNumericCellValue();
                    maxPointsReason = null;
                } catch (IllegalStateException e) {
                    // not a numeric value; try to parse text / IRM / MaxPointsReason
                    rank = null;
                    maxPointsReason = rankOrMaxPointReasonCell.getStringCellValue();
                }
                final Cell scoreCellContents = row.getCell(columnNumberOfFirstRaceScore+raceNumber);
                double score = scoreCellContents == null ? 0.0 : scoreCellContents.getNumericCellValue();
                if (score != 0.0) {
                    CompetitorEntry entry = new DefaultCompetitorEntryImpl(rank,
                            maxPointsReason, score, /* discarded */ false);
                    rankAndMaxPointsReasonAndPointsAndDiscarded.add(entry);
                } else {
                    rankAndMaxPointsReasonAndPointsAndDiscarded.add(null);
                }
            }
            CompetitorRow competitorRow = new CompetitorRowImpl(totalRank, sailID, names, scoreAfterDiscarding,
                    totalPointsBeforeDiscarding, rankAndMaxPointsReasonAndPointsAndDiscarded);
            competitorRows.add(competitorRow);
            rowIndex++;
        }
        return new RegattaResults() {
            @Override
            public Map<String, String> getMetadata() {
                Map<String, String> result = new HashMap<>();
                result.put(ScoreCorrectionProviderImpl.BOATCLASS_NAME_METADATA_PROPERTY, "505");
                return result;
            }
            
            @Override
            public List<CompetitorRow> getCompetitorResults() {
                return competitorRows;
            }
        };
    }
}
