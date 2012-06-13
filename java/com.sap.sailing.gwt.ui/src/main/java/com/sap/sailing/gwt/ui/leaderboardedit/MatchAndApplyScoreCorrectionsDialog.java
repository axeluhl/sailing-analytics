package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO.ScoreCorrectionEntryDTO;

public class MatchAndApplyScoreCorrectionsDialog extends DataEntryDialog<ScoreCorrectionsApplicationInstructions> {
    private static final RegExp p = RegExp.compile("^([A-Z][A-Z][A-Z])[^0-9]*([0-9]*)$");

    private final LeaderboardDTO leaderboard;
    private final Map<String, CompetitorDTO> sailIDToCompetitor;
    private final Map<String, String> raceColumnNameToOfficialRaceNameOrNumber;
    private final RegattaScoreCorrectionDTO regattaScoreCorrection;

    public MatchAndApplyScoreCorrectionsDialog(LeaderboardDTO leaderboard, StringMessages stringMessages,
            SailingServiceAsync sailingService, ErrorReporter errorReporter, RegattaScoreCorrectionDTO result) {
        super(stringMessages.assignRaceNumbersToRaceColumns(), stringMessages.assignRaceNumbersToRaceColumns(),
                stringMessages.ok(), stringMessages.cancel(), new Validator(), new Callback());
        this.regattaScoreCorrection = result;
        this.leaderboard = leaderboard;
        this.sailIDToCompetitor = mapCompetitors(leaderboard, result);
        this.raceColumnNameToOfficialRaceNameOrNumber = createRaceColumnNameToOfficialRaceNameOrNumberSuggestion(leaderboard, result);
    }

    /**
     * The default suggestion made for the mapping of leaderboard race column names to the official race name/number scheme
     * is by their ordering.
     */
    private Map<String, String> createRaceColumnNameToOfficialRaceNameOrNumberSuggestion(
            LeaderboardDTO leaderboard, RegattaScoreCorrectionDTO regattaScoreCorretion) {
        Map<String, String> result = new HashMap<String, String>();
        Iterator<RaceColumnDTO> raceColumnIter = leaderboard.getRaceList().iterator();
        Iterator<String> officialRaceNameOrNumberIter = regattaScoreCorretion.getScoreCorrectionsByRaceNameOrNumber().keySet().iterator();
        while (raceColumnIter.hasNext() && officialRaceNameOrNumberIter.hasNext()) {
            result.put(raceColumnIter.next().name, officialRaceNameOrNumberIter.next());
        }
        return result;
    }

    /**
     * Maps the sail IDs contained in <code>result</code> to the {@link CompetitorDTO}s contained in <code>leaderboard</code>.
     * The match making ignores all whitespaces in the sail IDs on both sides. If the {@link CompetitorDTO#sailID} does not start
     * with a letter it is assumed the country code is missing. In this case, the {@link CompetitorDTO#threeLetterIocCountryCode} is
     * prepended before comparing to <code>result</code>'s sail IDs. The sail ID number is extracted by trimming and using all
     * trailing digits.
     * 
     * @return a map mapping the sailIDs as found in <code>result</code> to the {@link CompetitorDTO}s used in <code>leaderboard</code>
     */
    private Map<String, CompetitorDTO> mapCompetitors(LeaderboardDTO leaderboard, RegattaScoreCorrectionDTO regattaScoreCorrection) {
        Map<String, CompetitorDTO> result = new HashMap<String, CompetitorDTO>();
        Map<String, CompetitorDTO> canonicalizedLeaderboardSailIDToCompetitors = canonicalizeLeaderboardSailIDs(leaderboard);
        for (Map<String, ScoreCorrectionEntryDTO> scoreCorrectionsBySailID : regattaScoreCorrection.getScoreCorrectionsByRaceNameOrNumber().values()) {
            for (String resultSailID : scoreCorrectionsBySailID.keySet()) {
                String canonicalizedResultSailID = canonicalizeSailID(resultSailID, /* defaultNationality */ null);
                CompetitorDTO competitor = canonicalizedLeaderboardSailIDToCompetitors.get(canonicalizedResultSailID);
                if (competitor != null) {
                    result.put(resultSailID, competitor);
                }
            }
        }
        return result;
    }
    
    private String canonicalizeSailID(String sailID, String defaultNationality) {
        String result = null;
        MatchResult m = p.exec(sailID);
        if (p.test(sailID)) {
            String iocCode = m.getGroup(1);
            if (defaultNationality != null && (iocCode == null || iocCode.trim().length() == 0)) {
                iocCode = defaultNationality;
            }
            if (iocCode != null && iocCode.trim().length() > 0) {
                String number = m.getGroup(2);
                result = iocCode + number;
            }
        }
        return result;
    }

    private Map<String, CompetitorDTO> canonicalizeLeaderboardSailIDs(LeaderboardDTO leaderboard) {
        Map<String, CompetitorDTO> result = new HashMap<String, CompetitorDTO>();
        for (CompetitorDTO competitor : leaderboard.competitors) {
            String canonicalizedSailID = canonicalizeSailID(competitor.sailID.trim(), competitor.threeLetterIocCountryCode.trim());
            if (canonicalizedSailID != null) {
                result.put(canonicalizedSailID, competitor);
            }
        }
        return result;
    }

    @Override
    protected ScoreCorrectionsApplicationInstructions getResult() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        // Create a grid that hosts races on X and competitors on Y with the leaderboard ones as fixed entries and
        // the correction ones as selectable drop-downs. The grid cells show the current leaderboard entries
        // and the suggested correction plus a checkbox each. Each row and each column has a checkbox as well.
        // If a row/column checkbox is toggled, it sets all checkboxes in the row/column to the new state.
        // When OKing the dialog, for all ticked cells the corrections are applied.
        Grid grid = new Grid(leaderboard.competitors.size()+1, leaderboard.getRaceList().size()+1);
        int c = 0;
        for (RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
            grid.setWidget(0, c, /* TODO */ new Label(raceColumn.name+" vs. "+raceColumnNameToOfficialRaceNameOrNumber.get(raceColumn.name)));
            c++;
        }
        int row=1;
        for (Map.Entry<String, CompetitorDTO> officialSailIDAndCompetitor : sailIDToCompetitor.entrySet()) {
            int column = 0;
            grid.setWidget(row, column++, new Label(officialSailIDAndCompetitor.getKey()+"/"+officialSailIDAndCompetitor.getValue().sailID));
            LeaderboardRowDTO leaderboardRow = leaderboard.rows.get(officialSailIDAndCompetitor.getValue());
            for (RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
                LeaderboardEntryDTO entry = leaderboardRow.fieldsByRaceColumnName.get(raceColumn.name);
                ScoreCorrectionEntryDTO officialCorrectionEntry =
                        regattaScoreCorrection.getScoreCorrectionsByRaceNameOrNumber()
                        .get(raceColumnNameToOfficialRaceNameOrNumber.get(raceColumn.name))
                        .get(officialSailIDAndCompetitor.getKey());
                grid.setWidget(row, column++, new Label(entry.netPoints+"/"+entry.totalPoints+"/"+entry.reasonForMaxPoints+
                (entry.discarded?"/discarded":""+" vs. "+officialCorrectionEntry.getScore()+"/"+officialCorrectionEntry.getMaxPointsReason()+
                        (officialCorrectionEntry.getDiscarded()?"/discarded":""))));
            }
            row++;
        }
        return grid;
    }

    private static class Validator implements DataEntryDialog.Validator<ScoreCorrectionsApplicationInstructions> {
        @Override
        public String getErrorMessage(ScoreCorrectionsApplicationInstructions valueToValidate) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static class Callback implements AsyncCallback<ScoreCorrectionsApplicationInstructions> {
        @Override
        public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSuccess(ScoreCorrectionsApplicationInstructions result) {
            // TODO Auto-generated method stub
        }
    }
}
