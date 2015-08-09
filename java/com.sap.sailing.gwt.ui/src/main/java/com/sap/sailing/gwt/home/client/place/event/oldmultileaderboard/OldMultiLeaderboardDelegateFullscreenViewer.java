package com.sap.sailing.gwt.home.client.place.event.oldmultileaderboard;

import com.sap.sailing.gwt.home.client.place.event.oldleaderboards.AbstractLeaderboardFullscreenViewer;
import com.sap.sailing.gwt.home.client.place.event.oldmultileaderboard.OldMultiLeaderboard.OldMultiLeaderboardDelegate;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardPanel;

public class OldMultiLeaderboardDelegateFullscreenViewer extends
        AbstractLeaderboardFullscreenViewer<MultiLeaderboardPanel> implements OldMultiLeaderboardDelegate {
    
    public OldMultiLeaderboardDelegateFullscreenViewer() {
        setHeaderWidget(createPanel(lastScoringComment, scoringScheme));
    }
}