package com.sap.sailing.dashboards.gwt.client.device;

import java.util.ArrayList;
import java.util.List;

/**
 * The class is a Singleton that that notifies it registered CompassListeners about every compass heading change, if it
 * is available at the current device. It uses JSNI to receive compass events with the HTML5 DeviceOrientation API.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class Compass {

    private static Compass INSTANCE = null;
    private List<CompassListener> compassListeners;

    private Compass() {
        initCompassEventListener(this);
    }

    public static Compass getInstance() {
        // No more tension of threads
        synchronized (Compass.class) {
            if (INSTANCE == null) {
                INSTANCE = new Compass();
            }
        }
        return INSTANCE;
    }

    private static native void initCompassEventListener(Compass compass) /*-{
		if (window.DeviceOrientationEvent) {
			window
					.addEventListener(
							'deviceorientation',
							function(e) {
								if (e != null) {
									var c = e.compassHeading
											|| e.webkitCompassHeading || 0;
									var accuracy = e.compassAccuracy
											|| e.webkitCompassAccuracy || 0;
									compass.@com.sap.sailing.dashboards.gwt.client.device.Compass::notifiyListenerAboutHeadingChange(DD)(c, accuracy);
								}
							}, false);
		} else {
			window.alert("Compass not supported for this device");
		}
    }-*/;

    public void addListener(CompassListener compassListener) {
        if (compassListeners == null) {
            compassListeners = new ArrayList<CompassListener>();
        }
        compassListeners.add(compassListener);
    }

    public void removeListener(CompassListener compassListener) {
        if (compassListeners != null) {
            compassListeners.remove(compassListener);
        }
    }

    public void notifiyListenerAboutHeadingChange(double newCompassHeading, double accuracy) {
        for (CompassListener compassListener : compassListeners) {
            compassListener.compassHeadingChanged(newCompassHeading);
        }
    }
}
