package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspective;

/**
 * A proxy perspective containing proxy components for all components of a race viewer with all components like map, leaderboard, charts, etc.  
 * @author Frank
 *
 */
public class ProxyRaceBoardPerspective extends AbstractPerspective<RaceBoardPerspectiveSettings> {
    private RaceBoardPerspectiveSettings perspectiveSettings;
    
    public ProxyRaceBoardPerspective(RaceBoardPerspectiveSettings perspectiveSettings, AbstractLeaderboardDTO leaderboard) {
        super();
        
        this.perspectiveSettings = perspectiveSettings;
        
        componentLifecycles.add(new RaceMapLifecycle(StringMessages.INSTANCE));
        componentLifecycles.add(new WindChartLifecycle(StringMessages.INSTANCE));
        componentLifecycles.add(new LeaderboardPanelLifecycle(leaderboard, StringMessages.INSTANCE));
        componentLifecycles.add(new MultiCompetitorRaceChartLifecycle(StringMessages.INSTANCE));
        componentLifecycles.add(new MediaPlayerLifecycle(StringMessages.INSTANCE));
    }

    @Override
    public String getPerspectiveName() {
        return "Race Viewer";
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
    public SettingsDialogComponent<RaceBoardPerspectiveSettings> getSettingsDialogComponent() {
        return new RaceBoardPerspectiveSettingsDialogComponent(perspectiveSettings, StringMessages.INSTANCE);
    }

    @Override
    public void updateSettings(RaceBoardPerspectiveSettings newSettings) {
        this.perspectiveSettings = newSettings;
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }

    @Override
    public RaceBoardPerspectiveSettings getSettings() {
        return perspectiveSettings;
    }
    
    @Override
    public void setSettingsOfComponents(CompositeSettings settingsOfComponents) {
        // no-op
    }
}
