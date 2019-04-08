package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sse.common.Util;

public abstract class AbstractRaceColumnSelection implements RaceColumnSelection {
    @Override
    public boolean isSelected(String raceColumnName) {
        return Util.contains(getSelectedRaceColumnNames(), raceColumnName);
    }

    @Override
    public Iterable<String> getSelectedRaceColumnNames() {
        List<String> names = new ArrayList<String>();
        for (RaceColumnDTO column : getSelectedRaceColumns()) {
            names.add(column.getName());
        }
        return names;
    }

    @Override
    public Iterable<RaceColumnDTO> getSelectedRaceColumnsOrderedAsInLeaderboard(LeaderboardDTO leaderboard) {
        List<String> namesOfSelectedRaceColumns = new ArrayList<String>();
        for (RaceColumnDTO selectedColumn : getSelectedRaceColumns()) {
            namesOfSelectedRaceColumns.add(selectedColumn.getName());
        }
        List<RaceColumnDTO> result = new ArrayList<RaceColumnDTO>();
        for (RaceColumnDTO column : leaderboard.getRaceList()) {
            if (Util.contains(namesOfSelectedRaceColumns, column.getName())) {
                result.add(column);
            }
        }
        return result;
    }

}
