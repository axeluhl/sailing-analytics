package com.sap.sailing.gwt.home.client.place.event.oldmultileaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface OldMultiLeaderboardResources extends ClientBundle {
    public static final OldMultiLeaderboardResources INSTANCE = GWT.create(OldMultiLeaderboardResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/oldmultileaderboard/OldMultiLeaderboard.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String oldMultiLeaderboardPanel();
    }
}
