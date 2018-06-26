package com.sap.sailing.racecommittee.app.ui.comparators;

import android.drm.DrmStore;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CompetitorGoalPassingComparator  implements Comparator<Map.Entry<Competitor, Boat>> {

    private ConcurrentMap<UUID, Integer> ranking;

    public CompetitorGoalPassingComparator() {
        this.ranking = new ConcurrentHashMap<>();
    }

    public ConcurrentMap updateWith(List<Util.Pair<Long, String>> sortedList) {
        ConcurrentMap result = new ConcurrentHashMap<>();

        Integer index = 0;
        for (Util.Pair<Long, String> entry : sortedList) {
            result.put(UUID.fromString(entry.getB()), index);
            index++;
        }
        this.ranking = result;
        return this.ranking;
    }

    @Override
    public int compare(Map.Entry<Competitor, Boat> leftCompetitor, Map.Entry<Competitor, Boat> rightCompetitor) {
        Integer leftRank = this.ranking.get(leftCompetitor.getKey().getId());
        Integer rightRank = this.ranking.get(rightCompetitor.getKey().getId());
        return leftRank - rightRank;
    }
}
