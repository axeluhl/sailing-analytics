package com.sap.sailing.dashboards.gwt.client.device;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util.Pair;

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
			if(orientationDegrees != null)
			orientationFetcher.@com.sap.sailing.dashboards.gwt.client.device.Orientation::receivedOrientationDegreesChange(I)(orientationDegrees);
		};
    }-*/;

    private void receivedOrientationDegreesChange(int orientationDegrees) {
        notifiyListenerAboutHeadingChange(orientationDegrees);
    }

    private static native void readDeviceOrientation(Orientation orientationFetcher) /*-{
		var orientationDegrees = window.orientation;
		if(orientationDegrees != null)
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
        Pair<OrientationType, Double> orienationPair = new Pair<OrientationType, Double>(getOrientationTypeForHeadingInSafari(orientationHeading), new Double(orientationHeading));
        for (OrientationListener orientationListener : orientationListeners) {
            orientationListener.orientationChanged(orienationPair);
        }
    }
    
    
    private OrientationType getOrientationTypeForHeadingInSafari(double heading){
        OrientationType orientationType = null;
        if (heading > -45 && heading <= 0 || heading > 0 && heading < 45){
            orientationType = OrientationType.PORTRAIT_UP;
        }else if (heading > 45 && heading < 135){
            orientationType = OrientationType.LANDSCAPE_RIGHT;
        }else if (heading > 135 && heading <= 180 || heading >=-180 && heading < -135){
            orientationType = OrientationType.PORTRAIT_DOWN;
        }else if (heading > -135 && heading < -45){
            orientationType = OrientationType.LANDSCAPE_LEFT;
        }
        return orientationType;
    }
    
    private OrientationType getOrientationTypeForHeadingInChrome(double heading){
        OrientationType orientationType = null;
        if (heading > -45 && heading <= 0 || heading > 0 && heading < 45){
            orientationType = OrientationType.LANDSCAPE_RIGHT;
        }else if (heading > 45 && heading < 135){
            orientationType = OrientationType.PORTRAIT_DOWN;
        }else if (heading > 135 && heading <= 180 || heading >=-180 && heading < -135){
            orientationType = OrientationType.LANDSCAPE_LEFT;
        }else if (heading > -135 && heading < -45){
            orientationType = OrientationType.PORTRAIT_UP;
        }
        return orientationType;
    }
}
