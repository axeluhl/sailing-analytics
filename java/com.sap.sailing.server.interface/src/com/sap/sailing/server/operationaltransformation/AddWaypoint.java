package com.sap.sailing.server.operationaltransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.Util;

public class AddWaypoint extends WaypointOperation {
    private static final Logger logger = Logger.getLogger(AddWaypoint.class.getName());
    private static final long serialVersionUID = -1965390080325959255L;
    private final Iterable<Waypoint> waypointsBeforeAdd;

    public AddWaypoint(RegattaAndRaceIdentifier raceIdentifier, int zeroBasedIndex, Waypoint waypoint, Iterable<Waypoint> waypointsAfterAdd) {
        super(raceIdentifier, zeroBasedIndex, waypoint);
        List<Waypoint> waypointsBeforeAdd = new ArrayList<>();
        for (Waypoint wp : waypointsAfterAdd) {
            if (wp != waypoint) {
                waypointsBeforeAdd.add(wp);
            }
        }
        this.waypointsBeforeAdd = waypointsBeforeAdd;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        RaceDefinition race = toState.getRace(getRaceIdentifier());
        final Course course = race.getCourse();
        final boolean apply;
        course.lockForRead();
        try {
            if (!Util.equals(course.getWaypoints(), waypointsBeforeAdd)) {
                apply = false;
                logger.info("Not applying operation " + AddWaypoint.class.getName() + "(" + getZeroBasedIndex() + ", "
                        + getWaypoint() + ") because the waypoint list " + waypointsBeforeAdd
                        + " to which this operation was originally applied differs from the waypoint list "
                        + course.getWaypoints() + " to which it would be applied now.");
            } else {
                apply = true;
            }
        } finally {
            course.unlockAfterRead();
        }
        if (apply) {
            course.addWaypoint(getZeroBasedIndex(), getWaypoint());
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
