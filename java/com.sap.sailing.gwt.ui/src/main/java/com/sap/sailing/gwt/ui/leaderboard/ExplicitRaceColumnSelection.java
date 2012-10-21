package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

public class ExplicitRaceColumnSelection implements RaceColumnSelection {
    private final Map<String, RaceColumnDTO> selectedRaceColumns;
    
    public ExplicitRaceColumnSelection() {
        this.selectedRaceColumns = new HashMap<String, RaceColumnDTO>();
    }
    
    @Override
    public Set<String> getSelectedRaceColumnNames() {
        return selectedRaceColumns.keySet();
    }

    @Override
    public boolean isSelected(String raceColumnName) {
        return selectedRaceColumns.containsKey(raceColumnName);
    }

    @Override
    public Iterable<RaceColumnDTO> getSelectedRaceColumns() {
        return selectedRaceColumns.values();
    }

    @Override
    public void requestRaceColumnSelection(String raceColumnName, RaceColumnDTO column) {
        selectedRaceColumns.put(raceColumnName, column);
    }

    @Override
    public void requestClear() {
        selectedRaceColumns.clear();
    }

    @Override
    public Iterable<RaceColumnDTO> getSelectedRaceColumnsOrderedAsInLeaderboard(LeaderboardDTO leaderboard) {
        List<RaceColumnDTO> result = new ArrayList<RaceColumnDTO>();
        for (RaceColumnDTO column : leaderboard.getRaceList()) {
            if (isSelected(column.name)) {
                result.add(column);
            }
        }
        return result;
    }
}
