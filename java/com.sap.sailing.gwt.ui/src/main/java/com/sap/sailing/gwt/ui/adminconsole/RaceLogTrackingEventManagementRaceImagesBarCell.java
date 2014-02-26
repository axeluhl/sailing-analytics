package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.ImagesBarCell;

public class RaceLogTrackingEventManagementRaceImagesBarCell extends ImagesBarCell {
    public final static String ACTION_ADD_RACELOG_TRACKER = "ACTION_ADD_RACELOG_TRACKER";
    public final static String ACTION_DENOTE_FOR_RACELOG_TRACKING = "ACTION_DENOTE_FOR_RACELOG_TRACKING";
    
    private final StringMessages stringMessages;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public RaceLogTrackingEventManagementRaceImagesBarCell(StringMessages stringConstants) {
        super();
        this.stringMessages = stringConstants;
    }
 
    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        List<ImageSpec> result = new ArrayList<ImageSpec>();
        RaceColumnDTOAndFleetDTOWithNameBasedEquality object = (RaceColumnDTOAndFleetDTOWithNameBasedEquality) getContext().getKey();
        if (object.getB().raceLogTrackerCanBeAdded) {
            result.add(new ImageSpec(ACTION_ADD_RACELOG_TRACKER, stringMessages.addRaceLogTracker(), makeImagePrototype(resources.addRaceLogTracker())));
        }
        if (! object.getB().raceLogTrackingState.isForTracking()) {
            result.add(new ImageSpec(ACTION_DENOTE_FOR_RACELOG_TRACKING, stringMessages.denoteForRaceLogTracking(), makeImagePrototype(resources.denoteForRaceLogTracking())));
        }
        return result;
    }
}