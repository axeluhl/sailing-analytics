package com.sap.sailing.domain.racelog.tracking.impl;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.racelog.tracking.RevokeEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceLogListener;

/**
 * Handles race creation upon receiving a {@link StartTrackingEvent}.
 * 
 * Note that the start time is expected to be updated to the {@link TrackedRace} by the
 * {@link DynamicTrackedRaceLogListener} which by default should be observing the race log for each tracked race.
 * 
 * @author Julian Gimbel (D056878)
 * 
 */
public class GenericRaceLogListener implements RaceLogEventVisitor {
    private static final Logger logger = Logger.getLogger(GenericRaceLogListener.class.getName());
    private final RaceLogRaceTracker tracker;

    public GenericRaceLogListener(RaceLogRaceTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void visit(RaceLogFlagEvent event) {
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
    }

    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
    }

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
    }

	@Override
	public void visit(DeviceCompetitorMappingEvent event) {
	}

	@Override
	public void visit(DeviceMarkMappingEvent event) {
	}

	@Override
	public void visit(DenoteForTrackingEvent event) {
	}

	@Override
	public void visit(StartTrackingEvent event) {
        tracker.onRaceCreated();
	}

	@Override
	public void visit(RevokeEvent event) {
	}
}
