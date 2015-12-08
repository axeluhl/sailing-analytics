package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.place.start.ProxyLeaderboardComponent;
import com.sap.sailing.gwt.autoplay.client.place.start.ProxyRaceMapComponent;
import com.sap.sailing.gwt.autoplay.client.place.start.ProxyWindChartComponent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartSettings;
import com.sap.sailing.gwt.ui.client.shared.perspective.AbstractPerspective;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A proxy perspective containing proxy components for all components of a race viewer with all components like map, leaderboard, charts, etc.  
 * @author Frank
 *
 */
public class ProxyRaceBoardPerspective extends AbstractPerspective<RaceBoardPerspectiveSettings> {
    private RaceBoardPerspectiveSettings perspectiveSettings;
    
    public ProxyRaceBoardPerspective(RaceBoardPerspectiveSettings perspectiveSettings, AbstractLeaderboardDTO leaderboard, LeaderboardSettings defaultLeaderboardSettings) {
        super();
        
        this.perspectiveSettings = perspectiveSettings;
        
        components.add(new ProxyRaceMapComponent(new RaceMapSettings(), StringMessages.INSTANCE));
        components.add(new ProxyWindChartComponent(new WindChartSettings(), StringMessages.INSTANCE));
        components.add(new ProxyLeaderboardComponent(defaultLeaderboardSettings, leaderboard, StringMessages.INSTANCE));
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
}
