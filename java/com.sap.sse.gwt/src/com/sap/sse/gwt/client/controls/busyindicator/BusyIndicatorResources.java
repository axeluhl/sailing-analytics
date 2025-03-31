package com.sap.sse.gwt.client.controls.busyindicator;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface BusyIndicatorResources extends ClientBundle {

    @Source("busy_indicator_circle.gif")
    ImageResource busyIndicatorCircle();

    @Source("busy_indicator_circle_inverted.gif")
    ImageResource busyIndicatorCircleInverted();
}
