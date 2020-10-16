package com.sap.sailing.util;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sse.common.Distance;

public class RegattaUtil {

    public static final Distance DEFAULT_BUOY_ZONE_RADIUS = new MeterDistance(15);

    public static Distance getCalculatedRegattaBuoyZoneRadius(Regatta regatta, BoatClass boatClass) {
        final double buoyZoneRadiusInHullLengths;
        if (regatta != null && regatta.getBuoyZoneRadiusInHullLengths() != null) {
            buoyZoneRadiusInHullLengths = regatta.getBuoyZoneRadiusInHullLengths();
        } else {
            buoyZoneRadiusInHullLengths = Regatta.DEFAULT_BUOY_ZONE_RADIUS_IN_HULL_LENGTHS;
        }
        final Distance boatHullLength = boatClass == null ? null : boatClass.getHullLength();
        final Distance buyZoneRadius = boatHullLength == null ? DEFAULT_BUOY_ZONE_RADIUS
                : boatHullLength.scale(buoyZoneRadiusInHullLengths);
        return buyZoneRadius;
    }
    
    public static Iterable<Regatta> getRegattasByEvent(Event event) {
        Set<Regatta> regattas = new HashSet<Regatta>();
        Iterable<LeaderboardGroup> leaderboardGroups = event.getLeaderboardGroups();
        for(LeaderboardGroup lbg:leaderboardGroups) {
            Iterable<Leaderboard> leaderboards = lbg.getLeaderboards();
            for(Leaderboard lb:leaderboards) {
                if(lb instanceof RegattaLeaderboard) {
                    Regatta regatta = ((RegattaLeaderboard) lb).getRegatta();
                    regattas.add(regatta);
                }
            }
        }
        return regattas;
    }
}
