package com.sap.sse.gwt.client.mvp.example;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeView;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeViewImpl;
import com.sap.sse.gwt.client.mvp.example.hello.HelloView;
import com.sap.sse.gwt.client.mvp.example.hello.HelloViewImpl;

public class ClientFactoryImpl implements ClientFactory {
    private static final EventBus eventBus = new SimpleEventBus();
    private static final PlaceController placeController = new PlaceController(eventBus);
    
    private HelloView helloView;
    private GoodbyeView goodbyeView;

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public PlaceController getPlaceController() {
        return placeController;
    }

    @Override
    public HelloView getHelloView() {
        if (helloView == null) {
            helloView = new HelloViewImpl();
        }
        return helloView;
    }

    @Override
    public GoodbyeView getGoodbyeView() {
        if (goodbyeView == null) {
            goodbyeView = new GoodbyeViewImpl();
        }
        return goodbyeView;
    }

}
