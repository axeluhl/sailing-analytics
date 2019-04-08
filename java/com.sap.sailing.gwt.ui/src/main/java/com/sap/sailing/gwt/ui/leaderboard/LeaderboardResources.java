package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface LeaderboardResources extends ClientBundle {
    @Source("com/sap/sailing/gwt/ui/client/images/arrow-gain.png")
    ImageResource arrowGainIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/arrow-loss.png")
    ImageResource arrowLossIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/arrow-gain-loss.png")
    ImageResource arrowGainLossIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/autorefresh_enabled.png")
    ImageResource autoRefreshEnabledIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/autorefresh_disabled.png")
    ImageResource autoRefreshDisabledIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/play.png")
    ImageResource playIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/pause.png")
    ImageResource pauseIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/chart.png")
    ImageResource chartIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/rankchart.png")
    ImageResource rankChartIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/magnifier.png")
    ImageResource magnifierIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/plusicon.png")
    ImageResource plusIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/minusicon.png")
    ImageResource minusIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/magnifier_slashed.png")
    ImageResource magnifierSlashedIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/jump_forward_button_enabled.png")
    ImageResource jumpForwardButtonEnabled();

    @Source("com/sap/sailing/gwt/ui/client/images/jump_forward_button_disabled.png")
    ImageResource jumpForwardButtonDisabled();
    
    @Source("medal_small.png")
    ImageResource medalSmall();

}