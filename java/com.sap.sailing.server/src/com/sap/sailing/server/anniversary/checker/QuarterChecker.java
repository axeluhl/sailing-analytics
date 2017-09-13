package com.sap.sailing.server.anniversary.checker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.anniversary.AnniversaryRaceDeterminator.AnniversaryChecker;

/**
 * This checker adds all races that are the 10kth,25kth,50kth or 75kth or are a factor x*10 of one of those.
 */
public class QuarterChecker implements AnniversaryChecker {
    private final static int[] ANNIVERSARIES = new int[] { 10000, 25000, 50000, 75000 };

    private final CopyOnWriteArrayList<Integer> pastAnniversaries;
    private int nextAnniversary;

    public QuarterChecker() {
        pastAnniversaries = new CopyOnWriteArrayList<>();
    }

    @Override
    public void update(int raceCount) {
        if (raceCount < 0) {
            throw new IllegalStateException("Negative Raceamount " + raceCount);
        }
        pastAnniversaries.clear();
        int factor = 1;
        // loops over all candidates as long as the race number is larger than the last tested candidate, terminates
        // once a candidate larger is found.
        while (true) {
            for (int anniversary : ANNIVERSARIES) {
                int anniversaryToCheck = anniversary * factor;
                if (anniversaryToCheck < 0) {
                    throw new IllegalStateException(
                            "overflow safeguard " + anniversary + " " + factor + " " + anniversaryToCheck);
                }
                if (anniversaryToCheck > raceCount) {
                    nextAnniversary = anniversaryToCheck;
                    return;
                }
                if (raceCount >= anniversaryToCheck) {
                    pastAnniversaries.add(anniversaryToCheck);
                }
            }
            factor *= 10;
        }
    }

    @Override
    public List<Integer> getAnniversaries() {
        return pastAnniversaries;
    }

    @Override
    public Integer getNextAnniversary() {
        return nextAnniversary;
    }

    @Override
    public AnniversaryType getType() {
        return AnniversaryType.QUARTER;
    }

}
