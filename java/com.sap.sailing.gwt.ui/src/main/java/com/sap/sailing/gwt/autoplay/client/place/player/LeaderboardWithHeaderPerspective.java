package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;

/**
 * A perspective managing a header with a single leaderboard filling the rest of the screen.
 * @author Frank
 *
 */
public class LeaderboardWithHeaderPerspective extends AbstractPerspectiveComposite<LeaderboardPerspectiveSettings> {
    private LeaderboardPerspectiveSettings settings;
    
    public LeaderboardWithHeaderPerspective(LeaderboardPerspectiveSettings perspectiveSettings, AbstractLeaderboardDTO leaderboard,
            StringMessages stringMessages) {
        super();
        this.settings = perspectiveSettings;
        componentLifecycles.add(new LeaderboardPanelLifecycle(leaderboard, stringMessages));
        componentLifecycles.add(new SAPHeaderLifecycle());
    }

    @Override
    public String getPerspectiveName() {
        return StringMessages.INSTANCE.leaderboard() + " Viewer";
    }

    @Override
    public String getLocalizedShortName() {
        return getPerspectiveName();
    }

    @Override
    public Widget getEntryWidget() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<LeaderboardPerspectiveSettings> getSettingsDialogComponent() {
        return new LeaderboardPerspectiveSettingsDialogComponent(settings, StringMessages.INSTANCE);
    }

    @Override
    public LeaderboardPerspectiveSettings getSettings() {
        return settings;
    }

    @Override
    public void updateSettings(LeaderboardPerspectiveSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }

    @Override
    public void setSettingsOfComponents(CompositeSettings settingsOfComponents) {
        // no-op
    }
}
