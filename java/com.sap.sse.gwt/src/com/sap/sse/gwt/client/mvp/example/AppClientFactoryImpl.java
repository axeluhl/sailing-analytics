package com.sap.sse.gwt.client.mvp.example;

import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeView;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeViewImpl;
import com.sap.sse.gwt.client.mvp.example.hello.HelloView;
import com.sap.sse.gwt.client.mvp.example.hello.HelloViewImpl;

public class AppClientFactoryImpl extends ClientFactoryImpl implements AppClientFactory {
    private HelloView helloView;
    private GoodbyeView goodbyeView;

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
