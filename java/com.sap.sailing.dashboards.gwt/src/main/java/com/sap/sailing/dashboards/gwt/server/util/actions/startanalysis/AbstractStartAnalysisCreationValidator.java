package com.sap.sailing.dashboards.gwt.server.util.actions.startanalysis;

import java.util.Iterator;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

@GwtIncompatible
public abstract class AbstractStartAnalysisCreationValidator {
    
    protected static int MINIMUM_MARKPASSIINGS_AT_FIRST_MARK = 3;
    protected static long MINIMUM_RACE_PROGRESSION_IN_MILLISECONDS = 6*60*1000;
    
    protected static boolean threeCompetitorsPassedSecondWayPoint(TrackedRace trackedRace){
        Waypoint secondWaypoint = trackedRace.getRace().getCourse().getFirstLeg().getTo();
        Iterator<MarkPassing> markPassingsInOrder = trackedRace.getMarkPassingsInOrder(secondWaypoint).iterator();
        int markPassingsCounter = 0;
        while (markPassingsInOrder.hasNext() && markPassingsCounter < MINIMUM_MARKPASSIINGS_AT_FIRST_MARK) {
            markPassingsInOrder.next();
            markPassingsCounter ++;
        }
        return markPassingsCounter >= MINIMUM_MARKPASSIINGS_AT_FIRST_MARK ? true : false;
    }
    
    private static boolean competitorPassedSecondWayPoint(Competitor competitor, TrackedRace trackedRace){
        Waypoint secondWaypoint = trackedRace.getRace().getCourse().getFirstLeg().getTo();
        return trackedRace.getMarkPassing(competitor, secondWaypoint) == null ? false : true;
    }
    
    private static boolean gateStartGolfFlagIsDown(TrackedRace trackedRace){
        if(trackedRace.getGateStartGolfDownTime() != 0){
            return true;
        }else{
            TimePoint timeStartOfRace = trackedRace.getStartOfRace();
            TimePoint timeNewestEvent = trackedRace.getTimePointOfNewestEvent();
            if(timeStartOfRace != null && timeNewestEvent != null){
                if(timeStartOfRace.asMillis()+MINIMUM_RACE_PROGRESSION_IN_MILLISECONDS > timeNewestEvent.asMillis()){
                    return true;
                }
            }
        }
        return false;
    }
    
    protected static boolean raceProgressedFarEnough(Competitor competitor, TrackedRace trackedRace){
        final Boolean isGateStart = trackedRace.isGateStart();
        if(isGateStart == Boolean.TRUE){
            return gateStartGolfFlagIsDown(trackedRace);
        }else{
            return competitorPassedSecondWayPoint(competitor, trackedRace);
        }
    }
}
