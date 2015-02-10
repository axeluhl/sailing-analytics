package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.Perspective;

/**
 * A perspective containing only a leaderboardPanel
 * @author Frank
 *
 */
public class LeaderboardPerspective implements Perspective {
    private final List<Component<?>> components;
    private final LeaderboardPanel leaderboardPanel;
    
    public LeaderboardPerspective(LeaderboardPanel leaderboardPanel) {
        this.leaderboardPanel = leaderboardPanel;

        components = new ArrayList<Component<?>>();
    }

    @Override
    public Iterable<Component<?>> getComponents() {
        return components;
    }

    public LeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }

    @Override
    public String getPerspectiveName() {
        return "Leaderboard";
    }


}
