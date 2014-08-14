package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TabletAndDesktopLeaderboardView extends Composite implements LeaderboardView {
    private static LeaderboardPageViewUiBinder uiBinder = GWT.create(LeaderboardPageViewUiBinder.class);

    interface LeaderboardPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopLeaderboardView> {
    }

    public TabletAndDesktopLeaderboardView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
