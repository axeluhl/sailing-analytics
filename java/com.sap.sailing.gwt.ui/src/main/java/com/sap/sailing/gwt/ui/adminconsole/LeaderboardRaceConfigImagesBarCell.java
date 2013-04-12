package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LeaderboardRaceConfigImagesBarCell extends ImagesBarCell {
    public static final String ACTION_REMOVE = "ACTION_REMOVE";
    public static final String ACTION_UNLINK = "ACTION_UNLINK";
    public static final String ACTION_EDIT = "ACTION_EDIT";
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
        if (selectedLeaderboardProvider.getSelectedLeaderboard() != null &&
                !selectedLeaderboardProvider.getSelectedLeaderboard().isRegattaLeaderboard) {
            // race columns cannot be removed from a regatta leaderboard; they need to be removed from the regatta instead
            result.add(new ImageSpec(ACTION_REMOVE, stringMessages.actionRaceRemove(), makeImagePrototype(resources.removeIcon())));
        }
        return result;
    }
}