package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.settings.client.leaderboard.RaceColumnSelectionStrategies;

/**
 * An explicit selection of race columns. Requests to add to or clear the selection are respected. If an updated version
 * of the leaderboard is announced, race columns added are implicitly also added to this selection.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ExplicitRaceColumnSelection extends AbstractRaceColumnSelection implements RaceColumnSelection {
    private final Map<String, RaceColumnDTO> selectedRaceColumns;
    
    public ExplicitRaceColumnSelection() {
        this.selectedRaceColumns = new HashMap<String, RaceColumnDTO>();
    }
    
    @Override
    public Iterable<RaceColumnDTO> getSelectedRaceColumns() {
        return selectedRaceColumns.values();
    }

    @Override
    public void requestRaceColumnSelection(RaceColumnDTO column) {
        selectedRaceColumns.put(column.getName(), column);
    }

    @Override
    public void requestClear() {
        selectedRaceColumns.clear();
    }

    private List<RaceColumnDTO> getRacesAddedNew(LeaderboardDTO oldLeaderboard, LeaderboardDTO newLeaderboard) {
        List<RaceColumnDTO> result = new ArrayList<RaceColumnDTO>();
        for (RaceColumnDTO s : newLeaderboard.getRaceList()) {
            if (oldLeaderboard == null || !leaderboardContainsColumnNamed(oldLeaderboard, s.getRaceColumnName())) {
                result.add(s);
            }
        }
        return result;
    }

    private boolean leaderboardContainsColumnNamed(LeaderboardDTO leaderboard, String raceColumnName) {
        for (RaceColumnDTO column : leaderboard.getRaceList()) {
            if (column.getRaceColumnName().equals(raceColumnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Considers the {@link #preSelectedRace} field as follows: if <code>null</code>, all race columns that
     * <code>leaderboard</code> adds on top of the existing <code>oldLeaderboard</code> are returned.
     * Otherwise, the column list obtained as described before is filtered such that only columns pass whose race
     * identifier equals {@link #preSelectedRace}.
     * 
     * @param newLeaderboard
     *            the new leaderboard, not yet {@link #setLeaderboard(LeaderboardDTO) set}, so as to allow for a
     *            comparison
     */
    protected List<RaceColumnDTO> getRaceColumnsToAddImplicitly(LeaderboardDTO newLeaderboard, LeaderboardDTO oldLeaderboard) {
        List<RaceColumnDTO> columnsToAddImplicitly = getRacesAddedNew(oldLeaderboard, newLeaderboard);
        return columnsToAddImplicitly;
    }

    @Override
    public void autoUpdateRaceColumnSelectionForUpdatedLeaderboard(LeaderboardDTO oldLeaderboard, LeaderboardDTO newLeaderboard) {
        for (RaceColumnDTO selectedRaceColumn : getRaceColumnsToAddImplicitly(newLeaderboard, oldLeaderboard)) {
            requestRaceColumnSelection(selectedRaceColumn);
        }
    }

    @Override
    public RaceColumnSelectionStrategies getType() {
        return RaceColumnSelectionStrategies.EXPLICIT;
    }

    @Override
    public Integer getNumberOfLastRaceColumnsToShow() {
        return null;
    }

}
