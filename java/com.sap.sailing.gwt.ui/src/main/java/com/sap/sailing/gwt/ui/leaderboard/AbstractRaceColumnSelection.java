package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

public abstract class AbstractRaceColumnSelection implements RaceColumnSelection {
    @Override
    public boolean isSelected(String raceColumnName) {
        return Util.contains(getSelectedRaceColumnNames(), raceColumnName);
    }

    @Override
    public Iterable<String> getSelectedRaceColumnNames() {
        List<String> names = new ArrayList<String>();
        for (RaceColumnDTO column : getSelectedRaceColumns()) {
            names.add(column.name);
        }
        return names;
    }

    @Override
    public Iterable<RaceColumnDTO> getSelectedRaceColumnsOrderedAsInLeaderboard(LeaderboardDTO leaderboard) {
        Iterable<RaceColumnDTO> selectedColumns = getSelectedRaceColumns();
        List<RaceColumnDTO> result = new ArrayList<RaceColumnDTO>();
        for (RaceColumnDTO column : leaderboard.getRaceList()) {
            if (Util.contains(selectedColumns, column)) {
                result.add(column);
            }
        }
        return result;
    }

}
