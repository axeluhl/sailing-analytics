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
    public ProxyRaceBoardPerspective(AbstractLeaderboardDTO leaderboard, LeaderboardSettings defaultLeaderboardSettings) {
        super();
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Widget getEntryWidget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isVisible() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasSettings() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SettingsDialogComponent<RaceBoardPerspectiveSettings> getSettingsDialogComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateSettings(RaceBoardPerspectiveSettings newSettings) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getDependentCssClassName() {
        // TODO Auto-generated method stub
        return null;
    }
}
