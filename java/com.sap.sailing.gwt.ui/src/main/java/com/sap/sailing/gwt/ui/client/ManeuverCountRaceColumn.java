package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.LegEntryDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public class ManeuverCountRaceColumn extends FormattedDoubleLegDetailColumn {
    private final StringConstants stringConstants;
    private final RaceNameProvider raceNameProvider;

    public ManeuverCountRaceColumn(String title, CellTable<LeaderboardRowDAO> leaderboardTable,
            RaceNameProvider raceNameProvider, String headerStyle, String columnStyle, StringConstants stringConstants) {
        super(title, /*unit*/null, /* field */ null, /* decimals */ 0, leaderboardTable, headerStyle, columnStyle);
        this.stringConstants = stringConstants;
        this.raceNameProvider = raceNameProvider;
    }

    @Override
    protected String getTitle(LeaderboardRowDAO row) {
        Triple<Integer, Integer, Integer> tacksJibesAndPenaltyCircles = getTotalNumberOfTacksJibesAndPenaltyCircles(row);
        StringBuilder result = new StringBuilder();
        if (tacksJibesAndPenaltyCircles.getA() != null) {
            result.append(tacksJibesAndPenaltyCircles.getA());
            result.append(" ");
            result.append(stringConstants.tacks());
        }
        if (tacksJibesAndPenaltyCircles.getB() != null) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(tacksJibesAndPenaltyCircles.getB());
            result.append(" ");
            result.append(stringConstants.jibes());
        }
        if (tacksJibesAndPenaltyCircles.getC() != null) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(tacksJibesAndPenaltyCircles.getC());
            result.append(" ");
            result.append(stringConstants.penaltyCircles());
        }
        return result.toString();
    }
    
    private Triple<Integer, Integer, Integer> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardRowDAO row) {
        Integer totalNumberOfTacks = null;
        Integer totalNumberOfJibes = null;
        Integer totalNumberOfPenaltyCircles = null;
        LeaderboardEntryDAO fieldsForRace = row.fieldsByRaceName.get(getRaceName());
        if (fieldsForRace != null && fieldsForRace.legDetails != null) {
            for (LegEntryDAO legDetail : fieldsForRace.legDetails) {
                if (legDetail != null) {
                    if (legDetail.numberOfTacks != null) {
                        if (totalNumberOfTacks == null) {
                            totalNumberOfTacks = legDetail.numberOfTacks;
                        } else {
                            totalNumberOfTacks += legDetail.numberOfTacks;
                        }
                    }
                    if (legDetail.numberOfJibes != null) {
                        if (totalNumberOfJibes == null) {
                            totalNumberOfJibes = legDetail.numberOfJibes;
                        } else {
                            totalNumberOfJibes += legDetail.numberOfJibes;
                        }
                    }
                    if (legDetail.numberOfPenaltyCircles != null) {
                        if (totalNumberOfPenaltyCircles == null) {
                            totalNumberOfPenaltyCircles = legDetail.numberOfPenaltyCircles;
                        } else {
                            totalNumberOfPenaltyCircles += legDetail.numberOfPenaltyCircles;
                        }
                    }
                }
            }
        }
        return new Triple<Integer, Integer, Integer>(totalNumberOfTacks, totalNumberOfJibes, totalNumberOfPenaltyCircles);
    }

    @Override
    protected Double getFieldValue(LeaderboardRowDAO row) {
        Double result = null;
        Triple<Integer, Integer, Integer> tacksJibesAndPenalties = getTotalNumberOfTacksJibesAndPenaltyCircles(row);
        Integer totalNumberOfTacks = tacksJibesAndPenalties.getA();
        Integer totalNumberOfJibes = tacksJibesAndPenalties.getB();
        Integer totalNumberOfPenaltyCircles = tacksJibesAndPenalties.getC();
        if (totalNumberOfTacks != null) {
            result = (double) totalNumberOfTacks;
        }
        if (totalNumberOfJibes != null) {
            if (result == null) {
                result = (double) totalNumberOfJibes;
            } else {
                result += (double) totalNumberOfJibes;
            }
        }
        if (totalNumberOfPenaltyCircles != null) {
            if (result == null) {
                result = (double) totalNumberOfPenaltyCircles;
            } else {
                result += (double) totalNumberOfPenaltyCircles;
            }
        }
        return result;
    }

    private String getRaceName() {
        return raceNameProvider.getRaceName();
    }

}
