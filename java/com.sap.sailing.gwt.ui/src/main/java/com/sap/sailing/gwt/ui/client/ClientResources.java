package com.sap.sailing.gwt.ui.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ClientResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/play.png")
    ImageResource playIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-pause-icon.png")
    ImageResource timesliderPauseIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-play-active-icon.png")
    ImageResource timesliderPlayActiveIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-play-inactive-icon.png")
    ImageResource timesliderPlayInactiveIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-speed.png")
    ImageResource timesliderPlaySpeedIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-playstate-live-active.png")
    ImageResource timesliderPlayStateLiveActiveIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-playstate-live-inactive.png")
    ImageResource timesliderPlayStateLiveInactiveIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-playstate-replay-active.png")
    ImageResource timesliderPlayStateReplayActiveIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-playstate-replay-inactive.png")
    ImageResource timesliderPlayStateReplayInactiveIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/sap-logo-overlay.png")
    ImageResource sapLogoOverlay();

}