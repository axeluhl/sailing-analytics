package com.sap.sailing.server.anniversary.checker;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.server.anniversary.AnniversaryDeterminator.AnniversaryChecker;
import com.sap.sse.common.Util.Pair;

/**
 *  This checker adds all races that are the 10kth,25kth,50kth or 75kth or are a factor 10 of one of those. 
 */
public class QuarterChecker implements AnniversaryChecker {
    private final static int[] ANNIVERSARIES = new int[] { 10000, 25000, 50000, 75000 };

    @Override
    public List<Pair<Integer, SimpleRaceInfo>> determineAnniversaries(List<SimpleRaceInfo> raceinfos) {
        ArrayList<Pair<Integer, SimpleRaceInfo>> result = new ArrayList<>();

        int newAmount = raceinfos.size();
        int factor = 1;
        while (true) {
            for (int anniversary : ANNIVERSARIES) {
                int anniversaryToCheck = anniversary * factor;
                if (anniversaryToCheck > newAmount) {
                    return result;
                }
                if (newAmount > anniversaryToCheck) {
                    result.add(
                            new Pair<Integer, SimpleRaceInfo>(anniversaryToCheck, raceinfos.get(anniversaryToCheck)));
                }
            }
        }
    }

}
