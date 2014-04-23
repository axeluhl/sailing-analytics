package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.ImagesBarCell;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RaceLogTrackingEventManagementImagesBarCell extends ImagesBarCell {
    public static final String ACTION_DENOTE_FOR_RACELOG_TRACKING = "ACTION_DENOTE_FOR_RACELOG_TRACKING";
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private final StringMessages stringMessages;
    
    public RaceLogTrackingEventManagementImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public RaceLogTrackingEventManagementImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringConstants) {
        super();
        this.stringMessages = stringConstants;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        ArrayList<ImageSpec> result = new ArrayList<ImageSpec>();

        StrippedLeaderboardDTO leaderboard = (StrippedLeaderboardDTO) getContext().getKey();
        
        if (leaderboard.isDenotableForRaceLogTracking) {
            result.add(new ImageSpec(ACTION_DENOTE_FOR_RACELOG_TRACKING, stringMessages.denoteForRaceLogTracking(), makeImagePrototype(resources.denoteForRaceLogTracking())));
        }

        return result;
    }
}