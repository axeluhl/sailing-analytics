package com.sap.sailing.racecommittee.app.ui.comparators;

import android.text.TextUtils;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.util.NaturalComparator;

import java.util.Comparator;
import java.util.Map;

public class CompetitorShortNameComparator implements Comparator<Map.Entry<Competitor, Boat>> {

    private NaturalComparator comparator;

    public CompetitorShortNameComparator() {
        this.comparator = new NaturalComparator();
    }

    @Override
    public int compare(Map.Entry<Competitor, Boat> leftCompetitor, Map.Entry<Competitor, Boat> rightCompetitor) {
        if (leftCompetitor != null && leftCompetitor.getKey() != null && rightCompetitor != null
                && rightCompetitor.getKey() != null) {
            Competitor left = leftCompetitor.getKey();
            Competitor right = rightCompetitor.getKey();
            return compare(left.getShortName(), right.getShortName(), comparator);
        }
        return 0;
    }

    public static int compare(String leftShortName, String rightShortName,
                              Comparator<String> comparator) {
        if (TextUtils.isEmpty(leftShortName) && TextUtils.isEmpty(rightShortName)) {
            return 0;
        }
        if (TextUtils.isEmpty(leftShortName)) {
            return -1;
        }
        if (TextUtils.isEmpty(rightShortName)) {
            return 1;
        }
        return comparator.compare(leftShortName, rightShortName);
    }
}
