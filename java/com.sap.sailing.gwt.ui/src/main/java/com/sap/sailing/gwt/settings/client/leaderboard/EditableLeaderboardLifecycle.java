package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class EditableLeaderboardLifecycle extends  AbstractMultiRaceLeaderboardPanelLifecycle<EditableLeaderboardSettings> {

    public EditableLeaderboardLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard,
            Iterable<DetailType> availableDetailTypes) {
        super(leaderboard, stringMessages, availableDetailTypes);
    }

    public SettingsDialogComponent<EditableLeaderboardSettings> getPerspectiveOwnSettingsDialogComponent(
            EditableLeaderboardSettings settings) {
        return new EditableLeaderboardSettingsDialogComponent(settings, namesOfRaceColumns, stringMessages,
                availableDetailTypes, canBoatInfoBeShown);
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.editableLeaderboardPage();
    }

    @Override
    public EditableLeaderboardSettings createDefaultSettings() {
        EditableLeaderboardSettings leaderboardSettings = new EditableLeaderboardSettings(DeviceDetector.isDesktop());
        SettingsUtil.copyDefaultsFromValues(leaderboardSettings, leaderboardSettings);
        return leaderboardSettings;
    }

    @Override
    public SettingsDialogComponent<EditableLeaderboardSettings> getSettingsDialogComponent(
            EditableLeaderboardSettings settings) {
        return new EditableLeaderboardSettingsDialogComponent(settings, namesOfRaceColumns, stringMessages,
                availableDetailTypes, canBoatInfoBeShown);
    }

    @Override
    public EditableLeaderboardSettings extractUserSettings(EditableLeaderboardSettings currentLeaderboardSettings) {
        return currentLeaderboardSettings;
    }

}
