package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

interface LeaderboardResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/settings.png")
    ImageResource settingsIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/play.png")
    ImageResource playIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/pause.png")
    ImageResource pauseIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/chart.png")
    ImageResource chartIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/magnifier.png")
    ImageResource magnifierIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/magnifier_slashed.png")
    ImageResource magnifierSlashedIcon();
}