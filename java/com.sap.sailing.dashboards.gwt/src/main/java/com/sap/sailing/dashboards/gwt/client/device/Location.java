package com.sap.sailing.dashboards.gwt.client.device;

import java.util.ArrayList;
import java.util.List;

/**
 * The class is a Singleton that that notifies it registered LocationListeners
 * about every location change, if it is available at the current device.
 * It uses JSNI to receive location events with Java Script.
 * 
 * @author Alexander Ries
 * 
 */
public class Location {

    private static Location INSTANCE = null;
    private List<LocationListener> locationListeners;

    private Location(){
        initLocationListener(this);
    }
    
    public static Location getInstance() {
        // No more tension of threads
        synchronized (Location.class) {
            if (INSTANCE == null) {
                INSTANCE = new Location();
            }
        }
        return INSTANCE;
    }

    private static native void initLocationListener(Location location) /*-{
		if (navigator.geolocation) {
			navigator.geolocation.watchPosition(showPosition);
		} else {
			window.alert("no position supported");
		}
		function showPosition(position) {
			if (position != null) {
				var lat = position.coords.latitude;
				var lon = position.coords.longitude
				location.@com.sap.sailing.dashboards.gwt.client.device.Location::notifiyListenerAboutLocationChange(DD)(lat, lon);
			}
		}
    }-*/;

    public void addListener(LocationListener locationListener) {
        if (locationListeners == null) {
            locationListeners = new ArrayList<LocationListener>();
        }
        locationListeners.add(locationListener);
    }

    public void removeListener(LocationListener locationListener) {
        if (locationListeners != null) {
            locationListeners.remove(locationListener);
        }
    }

    public void notifiyListenerAboutLocationChange(double latDeg, double longDeg) {
        for (LocationListener locationListener : locationListeners) {
            locationListener.locationChanged(latDeg, longDeg);
        }
    }
}