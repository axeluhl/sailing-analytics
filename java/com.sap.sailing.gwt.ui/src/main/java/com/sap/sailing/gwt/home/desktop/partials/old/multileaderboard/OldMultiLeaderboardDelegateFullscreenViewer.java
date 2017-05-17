package com.sap.sailing.gwt.home.desktop.partials.old.multileaderboard;

import com.sap.sailing.gwt.home.desktop.partials.old.AbstractLeaderboardFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.partials.old.multileaderboard.OldMultiLeaderboard.OldMultiLeaderboardDelegate;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardProxyPanel;

public class OldMultiLeaderboardDelegateFullscreenViewer extends
        AbstractLeaderboardFullscreenViewer<MultiLeaderboardProxyPanel> implements OldMultiLeaderboardDelegate {
    
    public OldMultiLeaderboardDelegateFullscreenViewer() {
        setHeaderWidget(createPanel(lastScoringComment, scoringScheme));
    }
}