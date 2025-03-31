package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.LegEntryDTO;
import com.sap.sailing.gwt.ui.leaderboard.DetailTypeColumn.DataExtractor;

public abstract class AbstractLastLegDetailField<T extends Comparable<?>> implements DataExtractor<T, LeaderboardRowDTO> {
    @Override
    public final T get(LeaderboardRowDTO row) {
        T result = null;
        LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
        if (fieldsForRace != null && fieldsForRace.legDetails != null) {
            int lastLegIndex = fieldsForRace.legDetails.size() - 1;
            if (lastLegIndex >= 0) {
                LegEntryDTO lastLegDetail = fieldsForRace.legDetails.get(lastLegIndex);
                // competitor may be in leg prior to the one the leader is in; find competitors current leg
                while (lastLegDetail == null && lastLegIndex > 0) {
                    lastLegDetail = fieldsForRace.legDetails.get(--lastLegIndex);
                }
                if (lastLegDetail != null) {
                    if (lastLegDetail.finished) {
                        result = getAfterLastLegFinished(row);
                    } else {
                        result = getBeforeLastLegFinished(lastLegDetail);
                    }
                }
            }
        }
        return result;
    }

    protected abstract String getRaceColumnName();

    protected abstract T getBeforeLastLegFinished(LegEntryDTO currentLegDetail);

    protected abstract T getAfterLastLegFinished(LeaderboardRowDTO row);
}