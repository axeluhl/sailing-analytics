package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.place.start.LeaderboardSettingsDialog.ProxyLeaderboardComponent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.components.Component;

/**
 * A perspective containing only a LeaderboardPanel
 * @author Frank
 *
 */
public class LeaderboardPerspective implements Perspective {
    private final List<Component<?>> components;
    
    public LeaderboardPerspective() {
        components = new ArrayList<Component<?>>();
    }

    public void setLeaderboard(AbstractLeaderboardDTO leaderboard) {
        components.clear();
        components.add(new ProxyLeaderboardComponent(StringMessages.INSTANCE, leaderboard));
    }
    
    @Override
    public Iterable<Component<?>> getComponents() {
        return components;
    }

    @Override
    public String getPerspectiveName() {
        return StringMessages.INSTANCE.leaderboard();
    }


}
