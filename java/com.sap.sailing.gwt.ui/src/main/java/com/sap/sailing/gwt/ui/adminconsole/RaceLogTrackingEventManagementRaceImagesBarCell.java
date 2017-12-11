package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLog;
import com.sap.sailing.gwt.ui.adminconsole.AbstractLeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sse.common.Util.Pair;

public class RaceLogTrackingEventManagementRaceImagesBarCell extends ImagesBarCell {
    public final static String ACTION_DENOTE_FOR_RACELOG_TRACKING = "ACTION_DENOTE_FOR_RACELOG_TRACKING";
    public final static String ACTION_REMOVE_DENOTATION = "ACTION_REMOVE_DENOTATION";
    public final static String ACTION_COMPETITOR_REGISTRATIONS = "ACTION_COMPETITOR_REGISTRATIONS";
    public final static String ACTION_DEFINE_COURSE = "ACTION_DEFINE_COURSE";
    public final static String ACTION_COPY = "ACTION_COPY";
    public final static String ACTION_EDIT = "ACTION_EDIT";
    public final static String ACTION_REFRESH_RACELOG = "ACTION_REFRESH_RACE_LOG";
    public final static String ACTION_SET_STARTTIME = "ACTION_SET_STARTTIME";
    public final static String ACTION_SET_FINISHING_AND_FINISH_TIME = "ACTION_SET_FINISHING_AND_FINISH_TIME";
    public final static String ACTION_SHOW_RACELOG = "ACTION_SHOW_RACELOG";
    public final static String ACTION_SET_TRACKING_TIMES = "ACTION_SET_TRACKING_TIMES";
    public final static String ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING";
    public final static String ACTION_START_TRACKING = "ACTION_START_TRACKING";
    public final static String ACTION_EDIT_COMPETITOR_TO_BOAT_MAPPINGS = "ACTION_EDIT_COMPETITOR_TO_BOAT_MAPPINGS";
    
    private final StringMessages stringMessages;
    private SmartphoneTrackingEventManagementPanel smartphoneTrackingEventManagementPanel;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public RaceLogTrackingEventManagementRaceImagesBarCell(StringMessages stringConstants, SmartphoneTrackingEventManagementPanel smartphoneTrackingEventManagementPanel) {
        super();
        this.stringMessages = stringConstants;
        this.smartphoneTrackingEventManagementPanel = smartphoneTrackingEventManagementPanel;
    }
 
    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        List<ImageSpec> result = new ArrayList<ImageSpec>();
        RaceColumnDTOAndFleetDTOWithNameBasedEquality object = (RaceColumnDTOAndFleetDTOWithNameBasedEquality) getContext().getKey();
        if (! object.getA().getRaceLogTrackingInfo(object.getB()).raceLogTrackingState.isForTracking()) {
            result.add(new ImageSpec(ACTION_DENOTE_FOR_RACELOG_TRACKING, stringMessages.denoteForRaceLogTracking(), makeImagePrototype(resources.denoteForRaceLogTracking())));
        } else {
            result.add(new ImageSpec(ACTION_REMOVE_DENOTATION, stringMessages.removeDenotation(), makeImagePrototype(resources.unDenoteForRaceLogTracking())));
            result.add(new ImageSpec(ACTION_COMPETITOR_REGISTRATIONS, stringMessages.competitorRegistrations(), makeImagePrototype(resources.competitorRegistrations())));
            result.add(new ImageSpec(ACTION_DEFINE_COURSE, stringMessages.defineCourse(), makeImagePrototype(resources.defineCourse())));
            result.add(new ImageSpec(ACTION_COPY, stringMessages.copyCourseAndCompetitors(), makeImagePrototype(resources.copy())));
        }
        result.add(new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())));
        result.add(new ImageSpec(ACTION_REFRESH_RACELOG, stringMessages.refreshRaceLog(), makeImagePrototype(resources.reloadIcon())));
        result.add(new ImageSpec(ACTION_SET_STARTTIME, stringMessages.setStartTime(), makeImagePrototype(resources.clockIcon())));
        result.add(new ImageSpec(ACTION_SET_FINISHING_AND_FINISH_TIME, stringMessages.setFinishingAndFinishTime(), makeImagePrototype(resources.blueSmall())));
        result.add(new ImageSpec(ACTION_SHOW_RACELOG, stringMessages.raceLog(), makeImagePrototype(resources.flagIcon())));
        result.add(new ImageSpec(ACTION_SET_TRACKING_TIMES, stringMessages.setTrackingTimes(), makeImagePrototype(resources.setTrackingTimes())));
        
        Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> startEndTrackingTime = smartphoneTrackingEventManagementPanel.getTrackingTimesFor(object);
        if (startEndTrackingTime == null) {
            result.add(new ImageSpec(ACTION_START_TRACKING, stringMessages.startTracking(), makeImagePrototype(resources.startRaceLogTracking())));
        } else {
            if (startEndTrackingTime.getB() == null || startEndTrackingTime.getB().getTimePoint() == null) {
                result.add(new ImageSpec(ACTION_STOP_TRACKING, stringMessages.stopTracking(), makeImagePrototype(resources.stopRaceLogTracking())));
            }
        }
        
        if (smartphoneTrackingEventManagementPanel.getSelectedLeaderboard().canBoatsOfCompetitorsChangePerRace) {
            result.add(new ImageSpec(ACTION_EDIT_COMPETITOR_TO_BOAT_MAPPINGS, stringMessages.actionShowCompetitorToBoatAssignments(), makeImagePrototype(resources.sailboatIcon())));
        }
        
        return result;
    }
}