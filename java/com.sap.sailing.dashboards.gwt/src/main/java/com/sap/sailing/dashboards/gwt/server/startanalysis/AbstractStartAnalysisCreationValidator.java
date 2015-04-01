package com.sap.sailing.dashboards.gwt.server.startanalysis;

import java.util.Iterator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

public abstract class AbstractStartAnalysisCreationValidator {
    
    private static int MINIMUM_NUMBER_COMPETITORS_FOR_STARTANALYSIS = 3;
    private static long MINIMUM_RACE_PROGRESSION_IN_MILLISECONDS = 6*60*1000;
    
    protected boolean threeCompetitorsPassedSecondWayPoint(TrackedRace trackedRace){
        Waypoint secondWaypoint = trackedRace.getRace().getCourse().getFirstLeg().getTo();
        Iterator<MarkPassing> markPassingsInOrder = trackedRace.getMarkPassingsInOrder(secondWaypoint).iterator();
        int counter = 0;
        while (markPassingsInOrder.hasNext()) {
            markPassingsInOrder.next();
            counter ++;
        }
        return counter >= MINIMUM_NUMBER_COMPETITORS_FOR_STARTANALYSIS ? true : false;
    }
    
    private boolean competitorPassedSecondWayPoint(Competitor competitor, TrackedRace trackedRace){
        Waypoint secondWaypoint = trackedRace.getRace().getCourse().getFirstLeg().getTo();
        return trackedRace.getMarkPassing(competitor, secondWaypoint) == null ? false : true;
    }
    
    private boolean gateStartGolfFlagIsDown(TrackedRace trackedRace){
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
    
    protected boolean raceProgressedFarEnough(Competitor competitor, TrackedRace trackedRace){
        if(trackedRace.isGateStart()){
            return gateStartGolfFlagIsDown(trackedRace);
        }else{
            return competitorPassedSecondWayPoint(competitor, trackedRace);
        }
    }
}
