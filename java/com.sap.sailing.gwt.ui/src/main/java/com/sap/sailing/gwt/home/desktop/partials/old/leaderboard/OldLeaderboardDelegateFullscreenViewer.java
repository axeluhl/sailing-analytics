package com.sap.sailing.gwt.home.desktop.partials.old.leaderboard;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.home.desktop.partials.old.AbstractLeaderboardFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.partials.old.leaderboard.OldLeaderboard.OldLeaderboardDelegate;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;

public class OldLeaderboardDelegateFullscreenViewer extends AbstractLeaderboardFullscreenViewer<LeaderboardPanel>
        implements OldLeaderboardDelegate {
    
    private final Label hasLiveRace = new Label();
    
    public OldLeaderboardDelegateFullscreenViewer() {
        setHeaderWidget(createPanel(lastScoringComment, hasLiveRace, scoringScheme));
    }
    
    @Override
    public Element getHasLiveRaceElement() {
        return hasLiveRace.getElement();
    }
    
}