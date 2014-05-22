package com.sap.sse.gwt.client.mvp.example;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeView;
import com.sap.sse.gwt.client.mvp.example.hello.HelloView;

public interface ClientFactory {
    EventBus getEventBus();

    PlaceController getPlaceController();

    HelloView getHelloView();

    GoodbyeView getGoodbyeView();
}
