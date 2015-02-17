package com.sap.sailing.dashboards.gwt.server.startanalysis;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;

public abstract class AbstractStartAnalysisCreationValidator {
    
    private static int MINIMUM_NUMBER_COMPETITORS_FOR_STARTANALYSIS = 3;
    
    protected boolean threeCompetitorsPassedSecondWayPoint(TrackedRace trackedRace){
        Waypoint secondWaypoint = trackedRace.getRace().getCourse().getFirstLeg().getTo();
        List<MarkPassing> markPassingsInOrder = (List<MarkPassing>) trackedRace.getMarkPassingsInOrder(secondWaypoint);
        return markPassingsInOrder.size() >= MINIMUM_NUMBER_COMPETITORS_FOR_STARTANALYSIS ? true : false;
    }
    
    protected boolean competitorPassedSecondWayPoint(Competitor competitor, TrackedRace trackedRace){
        Waypoint secondWaypoint = trackedRace.getRace().getCourse().getFirstLeg().getTo();
        return trackedRace.getMarkPassing(competitor, secondWaypoint) == null ? false : true;
    }
}
