package com.sap.sailing.gwt.home.client.shared.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface OldLeaderboardResources extends ClientBundle {
    public static final OldLeaderboardResources INSTANCE = GWT.create(OldLeaderboardResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/leaderboard/OldLeaderboard.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String regattanavigation();
        String oldLeaderboardPanel();
    }
}
