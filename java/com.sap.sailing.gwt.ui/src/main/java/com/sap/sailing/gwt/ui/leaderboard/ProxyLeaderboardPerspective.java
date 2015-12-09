package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.place.start.ProxyLeaderboardComponent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.perspective.AbstractPerspective;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A proxy perspective containing a proxy component for the LeaderboardPanel
 * @author Frank
 *
 */
public class ProxyLeaderboardPerspective extends AbstractPerspective<LeaderboardPerspectiveSettings> {
    private LeaderboardPerspectiveSettings perspectiveSettings;
    
    public ProxyLeaderboardPerspective(LeaderboardPerspectiveSettings perspectiveSettings, AbstractLeaderboardDTO leaderboard, LeaderboardSettings defaultLeaderboardSettings) {
        super();
        this.perspectiveSettings = perspectiveSettings;
        components.add(new ProxyLeaderboardComponent(defaultLeaderboardSettings, leaderboard, StringMessages.INSTANCE));
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
}
