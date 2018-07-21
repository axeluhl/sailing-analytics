package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.IconResources;

public class LeaderboardRaceConfigImagesBarCell extends ImagesBarCell {
    public final static String ACTION_REMOVE = "ACTION_REMOVE";
    public final static String ACTION_UNLINK = "ACTION_UNLINK";
    public final static String ACTION_EDIT = "ACTION_EDIT";
    public final static String ACTION_REFRESH_RACELOG = "ACTION_REFRESH_RACE_LOG";
    public final static String ACTION_SET_STARTTIME = "ACTION_SET_STARTTIME";
    public final static String ACTION_SET_FINISHING_AND_FINISH_TIME = "ACTION_SET_FINISHING_AND_FINISH_TIME";
    public final static String ACTION_SHOW_RACELOG = "ACTION_SHOW_RACELOG";
    public final static String ACTION_EDIT_COMPETITOR_TO_BOAT_MAPPINGS = "ACTION_EDIT_COMPETITOR_TO_BOAT_MAPPINGS";
    
    private final StringMessages stringMessages;
    private final SelectedLeaderboardProvider selectedLeaderboardProvider;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public LeaderboardRaceConfigImagesBarCell(SelectedLeaderboardProvider selectedLeaderboardProvider, StringMessages stringConstants) {
        super();
        this.selectedLeaderboardProvider = selectedLeaderboardProvider;
        this.stringMessages = stringConstants;
    }
 
    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        List<ImageSpec> result = new ArrayList<ImageSpec>();
        result.add(new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())));
        result.add(new ImageSpec(ACTION_UNLINK, stringMessages.actionRaceUnlink(), makeImagePrototype(resources.unlinkIcon())));
        StrippedLeaderboardDTO selectedLeaderboard = selectedLeaderboardProvider.getSelectedLeaderboard();
        if (selectedLeaderboard != null && !selectedLeaderboard.type.isRegattaLeaderboard()) {
            // race columns cannot be removed from a regatta leaderboard; they need to be removed from the regatta instead
            result.add(new ImageSpec(ACTION_REMOVE, stringMessages.actionRaceRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())));
        }
        result.add(new ImageSpec(ACTION_REFRESH_RACELOG, stringMessages.refreshRaceLog(), makeImagePrototype(resources.reloadIcon())));
        result.add(new ImageSpec(ACTION_SET_STARTTIME, stringMessages.setStartTime(), makeImagePrototype(resources.clockIcon())));
        result.add(new ImageSpec(ACTION_SET_FINISHING_AND_FINISH_TIME, stringMessages.setFinishingAndFinishTime(), makeImagePrototype(resources.blueSmall())));
        result.add(new ImageSpec(ACTION_SHOW_RACELOG, stringMessages.raceLog(), makeImagePrototype(resources.flagIcon())));
        
        if (selectedLeaderboardProvider.getSelectedLeaderboard().canBoatsOfCompetitorsChangePerRace) {
            result.add(new ImageSpec(ACTION_EDIT_COMPETITOR_TO_BOAT_MAPPINGS, stringMessages.actionShowCompetitorToBoatAssignments(), makeImagePrototype(resources.sailboatIcon())));
        }
        
        return result;
    }
}