package com.sap.sse.gwt.client.mvp;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sse.gwt.client.mvp.example.AppClientFactory;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeActivityProxy;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyePlace;
import com.sap.sse.gwt.client.mvp.example.hello.HelloActivityProxy;
import com.sap.sse.gwt.client.mvp.example.hello.HelloPlace;

public class AppActivityMapper implements ActivityMapper {

    private AppClientFactory clientFactory;

    /**
     * AppActivityMapper associates each Place with its corresponding {@link Activity}
     * 
     * @param clientFactory
     *            Factory to be passed to activities
     */
    public AppActivityMapper(AppClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    /**
     * Map each Place to its corresponding Activity. This would be a great use for GIN.
     */
    @Override
    public Activity getActivity(final Place place) {
        // This is begging for GIN
        if (place instanceof HelloPlace) {
            return new HelloActivityProxy((HelloPlace) place, clientFactory);
        } else if (place instanceof GoodbyePlace) {
            return new GoodbyeActivityProxy((GoodbyePlace) place, clientFactory);
        }
        return null;
    }
}
