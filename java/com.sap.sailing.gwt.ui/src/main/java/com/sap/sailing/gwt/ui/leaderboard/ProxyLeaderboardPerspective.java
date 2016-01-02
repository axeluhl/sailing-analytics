package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspective;

/**
 * A proxy perspective containing a proxy component for the LeaderboardPanel
 * @author Frank
 *
 */
public class ProxyLeaderboardPerspective extends AbstractPerspective<LeaderboardPerspectiveSettings> {
    private LeaderboardPerspectiveSettings perspectiveSettings;
    
    public ProxyLeaderboardPerspective(LeaderboardPerspectiveSettings perspectiveSettings, AbstractLeaderboardDTO leaderboard) {
        super();
        this.perspectiveSettings = perspectiveSettings;
        componentLifecycles.add(new LeaderboardPanelLifecycle(leaderboard, StringMessages.INSTANCE));
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
        return new LeaderboardPerspectiveSettingsDialogComponent(perspectiveSettings, StringMessages.INSTANCE);
    }

    @Override
    public LeaderboardPerspectiveSettings getSettings() {
        return perspectiveSettings;
    }

    @Override
    public void updateSettings(LeaderboardPerspectiveSettings newSettings) {
        this.perspectiveSettings = newSettings;
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
