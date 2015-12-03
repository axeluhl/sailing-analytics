package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.place.start.ProxyLeaderboardComponent;
import com.sap.sailing.gwt.autoplay.client.place.start.ProxyRaceMapComponent;
import com.sap.sailing.gwt.autoplay.client.place.start.ProxyWindChartComponent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartSettings;
import com.sap.sailing.gwt.ui.client.shared.perspective.AbstractPerspective;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;

/**
 * A proxy perspective containing proxy components for all components of a race viewer with all components like map, leaderboard, charts, etc.  
 * @author Frank
 *
 */
public class ProxyRaceViewerPerspective extends AbstractPerspective {
    public ProxyRaceViewerPerspective(AbstractLeaderboardDTO leaderboard, LeaderboardSettings defaultLeaderboardSettings) {
        super();
        components.add(new ProxyRaceMapComponent(new RaceMapSettings(), StringMessages.INSTANCE));
        components.add(new ProxyWindChartComponent(new WindChartSettings(), StringMessages.INSTANCE));
        components.add(new ProxyLeaderboardComponent(defaultLeaderboardSettings, leaderboard, StringMessages.INSTANCE));
    }

    @Override
    public String getPerspectiveName() {
        return "Race Viewer";
    }
}
