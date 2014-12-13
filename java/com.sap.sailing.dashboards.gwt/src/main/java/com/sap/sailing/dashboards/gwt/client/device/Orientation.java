package com.sap.sailing.dashboards.gwt.client.device;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class Orientation {

    private static Orientation INSTANCE = null;
    private List<OrientationListener> orientationListeners;

    private Orientation() {
        initOrientationEventListener(this);
    }

    public static Orientation getInstance() {
        synchronized (Orientation.class) {
            if (INSTANCE == null) {
                INSTANCE = new Orientation();
            }
        }
        return INSTANCE;
    }

    private static native void initOrientationEventListener(Orientation orientationFetcher) /*-{
		window.onorientationchange = function() {
			var orientationDegrees = window.orientation;
			orientationFetcher.@com.sap.sailing.dashboards.gwt.client.device.Orientation::receivedOrientationDegreesChange(I)(orientationDegrees);
		};
    }-*/;

    private void receivedOrientationDegreesChange(int orientationDegrees) {
        notifiyListenerAboutHeadingChange(orientationDegrees);
    }

    private static native void readDeviceOrientation(Orientation orientationFetcher) /*-{
		var orientationDegrees = window.orientation;
		orientationFetcher.@com.sap.sailing.dashboards.gwt.client.device.Orientation::receivedOrientationDegreesChange(I)(orientationDegrees);
    }-*/;

    public void triggerDeviceOrientationRead() {
        readDeviceOrientation(this);
    }

    public void addListener(OrientationListener orientationListener) {
        if (orientationListeners == null) {
            orientationListeners = new ArrayList<OrientationListener>();
        }
        orientationListeners.add(orientationListener);
    }

    public void removeListener(OrientationListener orientationListener) {
        if (orientationListeners != null) {
            orientationListeners.remove(orientationListener);
        }
    }

    public void notifiyListenerAboutHeadingChange(double orientationHeading) {
        for (OrientationListener orientationListener : orientationListeners) {
            orientationListener.orientationChanged(orientationHeading);
        }
    }
}
