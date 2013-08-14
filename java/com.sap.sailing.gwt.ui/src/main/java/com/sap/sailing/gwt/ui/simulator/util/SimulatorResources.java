package com.sap.sailing.gwt.ui.simulator.util;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SimulatorResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/simulator/Windrose_plain_background.png")
    ImageResource windRoseBackground();

    @Source("com/sap/sailing/gwt/ui/client/images/simulator/Windrose_needle.png")
    ImageResource windRoseNeedle();

}
