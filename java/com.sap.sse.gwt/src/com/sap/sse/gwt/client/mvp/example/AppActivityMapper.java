package com.sap.sse.gwt.client.mvp.example;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeActivityProxy;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyePlace;
import com.sap.sse.gwt.client.mvp.example.hello.HelloActivityProxy;
import com.sap.sse.gwt.client.mvp.example.hello.HelloPlace;

public class AppActivityMapper implements ActivityMapper {
    private final AppClientFactory clientFactory;
    
    public AppActivityMapper(AppClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
    
    @Override
    public Activity getActivity(Place place) {
        if (place instanceof HelloPlace) {
            return new HelloActivityProxy((HelloPlace) place, clientFactory);
        } else if (place instanceof GoodbyePlace) {
            return new GoodbyeActivityProxy((GoodbyePlace) place, clientFactory);
        }
        return null;
    }

}
