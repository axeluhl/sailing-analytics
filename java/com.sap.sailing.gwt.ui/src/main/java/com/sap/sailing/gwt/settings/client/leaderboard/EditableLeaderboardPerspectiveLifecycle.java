package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class EditableLeaderboardPerspectiveLifecycle implements ComponentLifecycle<EditableLeaderboardSettings> {

    public static final String ID = "elbh";

    private final List<ComponentLifecycle<?>> componentLifecycles;
    private final StringMessages stringMessages;
    private final Iterable<DetailType> availableDetailTypes;
    private final List<String> namesOfRaceColumns;
    private final boolean canBoatInfoBeShown;

    public EditableLeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard,
            Iterable<DetailType> availableDetailTypes) {
        this(stringMessages, leaderboard, false, availableDetailTypes);
        addLifeCycle(new OverallLeaderboardPanelLifecycle(null, stringMessages, availableDetailTypes));
    }

    protected final void addLifeCycle(ComponentLifecycle<?> cycle) {
        for (ComponentLifecycle<?> old : componentLifecycles) {
            if (old.getComponentId().equals(cycle.getComponentId())) {
                throw new IllegalStateException("LifeCycle with duplicate ID " + cycle.getComponentId());
            }
        }
        componentLifecycles.add(cycle);
    }

    public Iterable<ComponentLifecycle<?>> getComponentLifecycles() {
        return componentLifecycles;
    }

    protected EditableLeaderboardPerspectiveLifecycle(StringMessages stringMessages, AbstractLeaderboardDTO leaderboard,
            boolean isOverall, Iterable<DetailType> availableDetailTypes) {
        this.stringMessages = stringMessages;
        this.availableDetailTypes = availableDetailTypes;
        if (leaderboard != null) {
            this.namesOfRaceColumns = leaderboard.getNamesOfRaceColumns();
            this.canBoatInfoBeShown = !leaderboard.canBoatsOfCompetitorsChangePerRace;
        } else {
            this.namesOfRaceColumns = new ArrayList<String>();
            this.canBoatInfoBeShown = false;
        }
        componentLifecycles = new ArrayList<>();
        addLifeCycle(new MultiRaceLeaderboardPanelLifecycle(leaderboard, stringMessages, availableDetailTypes));
        addLifeCycle(new MultiCompetitorLeaderboardChartLifecycle(isOverall));
    }

    public EditableLeaderboardSettings createPerspectiveOwnDefaultSettings() {
        return new EditableLeaderboardSettings(true);
    }

    public SettingsDialogComponent<EditableLeaderboardSettings> getPerspectiveOwnSettingsDialogComponent(
            EditableLeaderboardSettings settings) {
        return new EditableLeaderboardSettingsDialogComponent(settings, namesOfRaceColumns, stringMessages,
                availableDetailTypes, canBoatInfoBeShown);
    }

    protected EditableLeaderboardSettings extractOwnUserSettings(EditableLeaderboardSettings settings) {
        return settings;
    }

    protected EditableLeaderboardSettings extractOwnDocumentSettings(EditableLeaderboardSettings settings) {
        return settings;
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
    public boolean hasSettings() {
        return true;
    }

    @Override
    public String getComponentId() {
        return ID;
    }

}
