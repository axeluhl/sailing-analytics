package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.Util;

public class RemoveWaypoint extends WaypointOperation {
    private static final Logger logger = Logger.getLogger(RemoveWaypoint.class.getName());
    private static final long serialVersionUID = 7461197139000309999L;

    public RemoveWaypoint(RegattaAndRaceIdentifier raceIdentifier, int zeroBasedIndex, Waypoint waypoint) {
        super(raceIdentifier, zeroBasedIndex, waypoint);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        RaceDefinition race = toState.getRace(getRaceIdentifier());
        final Course course = race.getCourse();
        final boolean apply;
        course.lockForRead();
        try {
            final Waypoint waypointAtIndexInCourse = Util.get(course.getWaypoints(), getZeroBasedIndex());
            if (waypointAtIndexInCourse != getWaypoint()) {
                apply = false;
                logger.info("Not applying operation "+RemoveWaypoint.class.getName()+"("+getZeroBasedIndex()+", "+getWaypoint().getName()+
                        ") because the current course "+course+" contains waypoint "+waypointAtIndexInCourse+" at index "+getZeroBasedIndex()+
                        " which is different, so perhaps the initial load on this replica (is this a replica?) already contained the change.");
            } else {
                apply = true;
            }
        } finally {
            course.unlockAfterRead();
        }
        if (apply) {
            course.removeWaypoint(getZeroBasedIndex());
        }
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
