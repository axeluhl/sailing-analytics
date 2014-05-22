package com.sap.sse.gwt.client.mvp.example;

import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeView;
import com.sap.sse.gwt.client.mvp.example.hello.HelloView;

public interface AppClientFactory extends ClientFactory {
    HelloView getHelloView();

    GoodbyeView getGoodbyeView();
}
