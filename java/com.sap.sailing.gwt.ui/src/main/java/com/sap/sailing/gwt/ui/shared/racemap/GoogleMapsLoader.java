package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ScriptElement;

/**
 * The {@link #load(Runnable)} method can be used by clients to request the loading of the Google Maps API.
 * The callback passed will be invoked immediately if the API has already been loaded (e.g., by another
 * client call to the {@link #load(Runnable)} method within the same frame / document); it will be queued
 * for invocation by a Google Maps API callback function registered otherwise. This callback function
 * is injected at most once when the {@link #load(Runnable)} method is invoked for the first time and
 * will trigger all callbacks registered through the {@link #load(Runnable)} method until the maps API
 * invokes the callback registered.
 */
public class GoogleMapsLoader {
    /**
     * These params define the required information to authenticate with the Google Maps API.
     */
    public static final String AUTHENTICATION_PARAMS = "client=gme-sapglobalmarketing&channel=sapsailing.com";

    /**
     * Note: If you use 3, it will take the newest stable available. We want that, although we didn't test with that yet!
     * Google Release notes: https://developers.google.com/maps/documentation/javascript/releases.
     * Subscribe to https://groups.google.com/forum/#!forum/google-maps-js-api-v3-notify for change notifications.
     */
    public final static String API_VERSION = "3";
    
    /**
     * The required Google Maps libraries; a comma-separated list. See https://developers.google.com/maps/documentation/javascript/libraries
     * for more details. Examples: <tt>drawing,geometry,places,visualization</tt>
     */
    public final static String LIBRARIES = "drawing,geometry";
    
    private static boolean loading = false;
    private static boolean loaded = false;
    private static final Set<Runnable> callbacks = new HashSet<>();
    
    private GoogleMapsLoader() {
    }

    /**
     * @param callback must not be {@code null}.
     */
    public static void load(Runnable callback) {
        if (loaded) {
            Scheduler.get().scheduleDeferred(() -> callback.run());
        } else {
            callbacks.add(callback);
            if (!loading) {
                loading = true;
                installCallback();
                final ScriptElement scriptElement = Document.get().createScriptElement();
                scriptElement.setSrc("https://maps.googleapis.com/maps/api/js?v="+API_VERSION+"&" + AUTHENTICATION_PARAMS
                        + "&libraries="+LIBRARIES+"&callback=googleMapsLoadedCallback");
                Document.get().getHead().appendChild(scriptElement);
            }
        }
    }
    
    private static void callback() {
        loaded = true;
        loading = false;
        callbacks.forEach(Runnable::run);
        callbacks.clear();
        clearCallback();
    }
    
    private static native void installCallback() /*-{
        $wnd.googleMapsLoadedCallback = $entry(function() {
            @com.sap.sailing.gwt.ui.shared.racemap.GoogleMapsLoader::callback()();
        });
    }-*/;
    
    private static native void clearCallback() /*-{
        $wnd.googleMapsLoadedCallback = null;
    }-*/;
}
