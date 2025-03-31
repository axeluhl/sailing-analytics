package com.sap.sailing.gwt.ui.server;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.gwt.ui.shared.racemap.GoogleMapsLoader;

public class Activator implements BundleActivator {
    private static BundleContext context;
    private SailingServiceImpl sailingServiceToStopWhenStopping;
    private static Activator INSTANCE;
    
    private final static String GOOGLE_MAPS_LOADER_AUTHENTICATION_PARAMS_PROPERTY_NAME = "google.maps.authenticationparams";
    
    private final static String YOUTUBE_V3_API_KEY_PROPERTY_NAME = "youtube.api.key";
    
    /**
     * Required by {@link GoogleMapsLoader#load(Runnable, String)} and to be provided through a system property named
     * after {@link GOOGLE_MAPS_LOADER_AUTHENTICATION_PARAMS_PROPERTY_NAME}. The value would be something like
     * {@code client=abcde&channel=fghij}.
     */
    private String googleMapsLoaderAuthenticationParams;
    
    /**
     * A secret for accessing the YouTube V3 API; provided through the system property named as specified by
     * {@link #YOUTUBE_V3_API_KEY_PROPERTY_NAME}.
     */
    private String youtubeApiKey;

    public Activator() {
        INSTANCE = this;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        googleMapsLoaderAuthenticationParams = context.getProperty(GOOGLE_MAPS_LOADER_AUTHENTICATION_PARAMS_PROPERTY_NAME);
        youtubeApiKey = context.getProperty(YOUTUBE_V3_API_KEY_PROPERTY_NAME);
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (sailingServiceToStopWhenStopping != null) {
            sailingServiceToStopWhenStopping.stop();
        }
    }
    
    public static Activator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Activator();
        }
        return INSTANCE;
    }
    
    public static BundleContext getDefault() {
        return context;
    }
    
    /**
     * Returns a URL parameter string, e.g., like {@code client=abcde&channel=fghij}, provided to this activator through
     * a system property named after {@link GOOGLE_MAPS_LOADER_AUTHENTICATION_PARAMS_PROPERTY_NAME}. Won't be {@code null}
     * because the entire bundle won't activate if not set.
     */
    public String getGoogleMapsLoaderAuthenticationParams() {
        return googleMapsLoaderAuthenticationParams;
    }
    
    public String getYoutubeApiKey() {
        return youtubeApiKey;
    }

    public void setSailingService(SailingServiceImpl sailingServiceImpl) {
        sailingServiceToStopWhenStopping = sailingServiceImpl;
    }
}
