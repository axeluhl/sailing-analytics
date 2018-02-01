package com.sap.sailing.gwt.home.desktop.partials.old.multileaderboard;

import com.sap.sailing.gwt.home.desktop.partials.old.AbstractLeaderboardFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.partials.old.multileaderboard.OldMultiLeaderboard.OldMultiLeaderboardDelegate;

public class OldMultiLeaderboardDelegateFullscreenViewer extends
        AbstractLeaderboardFullscreenViewer implements OldMultiLeaderboardDelegate {
    
    public OldMultiLeaderboardDelegateFullscreenViewer() {
        setHeaderWidget(createPanel(lastScoringComment, scoringScheme));
    }
}