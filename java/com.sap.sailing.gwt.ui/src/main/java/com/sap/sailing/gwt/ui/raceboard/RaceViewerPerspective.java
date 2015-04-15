package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.place.start.LeaderboardSettingsDialog.ProxyLeaderboardComponent;
import com.sap.sailing.gwt.autoplay.client.place.start.RaceMapSettingsDialog.ProxyRaceMapComponent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.perspective.Perspective;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sse.gwt.client.shared.components.Component;

/**
 * A perspective containing a race viewer with all components like map, leaderboard, charts, etc.  
 * @author Frank
 *
 */
public class RaceViewerPerspective implements Perspective {
    private final List<Component<?>> components;
    
    public RaceViewerPerspective() {
        components = new ArrayList<Component<?>>();
    }

    public void setLeaderboard(AbstractLeaderboardDTO leaderboard) {
        components.clear();
        components.add(new ProxyRaceMapComponent(new RaceMapSettings(), StringMessages.INSTANCE));
        components.add(new ProxyLeaderboardComponent(StringMessages.INSTANCE, leaderboard));
    }
    
    @Override
    public Iterable<Component<?>> getComponents() {
        return components;
    }

    @Override
    public String getPerspectiveName() {
        return "RaceViewer";
    }


}
