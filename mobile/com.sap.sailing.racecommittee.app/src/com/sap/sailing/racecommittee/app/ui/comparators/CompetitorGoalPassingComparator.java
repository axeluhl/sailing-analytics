package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;

public class CompetitorGoalPassingComparator implements Comparator<Map.Entry<Competitor, Boat>> {

    private ConcurrentMap<UUID, Integer> ranking;
    private final NaturalComparator naturalComparator;

    public CompetitorGoalPassingComparator() {
        this.ranking = new ConcurrentHashMap<>();
        this.naturalComparator = new NaturalComparator();
    }

    public ConcurrentMap<UUID, Integer> updateWith(List<Util.Pair<Long, String>> sortedList) {
        ConcurrentMap<UUID, Integer> result = new ConcurrentHashMap<>();
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
        if (leftRank == null && rightRank == null) {
            //Compare further properties
            //Sail ID
            int result = CompetitorSailIdComparator.compare(leftCompetitor.getValue().getSailID(), rightCompetitor.getValue().getSailID(), naturalComparator);
            if (result == 0) {
                //Short name
                result = CompetitorShortNameComparator.compare(leftCompetitor.getKey().getShortName(), rightCompetitor.getKey().getShortName(), naturalComparator);
            }
            if (result == 0) {
                //Competitor name
                result = naturalComparator.compare(leftCompetitor.getKey().getName(), rightCompetitor.getKey().getName());
            }
            return result;
        }
        if (leftRank == null) {
            return 1;
        }
        if (rightRank == null) {
            return -1;
        }
        return leftRank - rightRank;
    }
}
