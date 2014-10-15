package com.sap.sailing.gwt.autoplay.client.shared.oldleaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface OldLeaderboardResources extends ClientBundle {
    public static final OldLeaderboardResources INSTANCE = GWT.create(OldLeaderboardResources.class);

    @Source("com/sap/sailing/gwt/autoplay/client/shared/oldleaderboard/OldLeaderboard.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String oldLeaderboardPanel();
        String leaderboardAndTitlePanel();
        String leaderboardAndTitleTable();
        String regattaleaderboard_meta_scoring_comment();
        String regattaleaderboard_meta_scoring_type();
        String regattaleaderboard_meta_update_text();
        String regattaleaderboard_meta_update_timestamp();
        
    }
}
