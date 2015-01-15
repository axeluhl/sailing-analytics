package com.sap.sse.gwt.client.mvp.example;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;
import com.sap.sse.gwt.client.mvp.TopLevelView;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeView;
import com.sap.sse.gwt.client.mvp.example.goodbye.GoodbyeViewImpl;
import com.sap.sse.gwt.client.mvp.example.hello.HelloPlace;
import com.sap.sse.gwt.client.mvp.example.hello.HelloView;
import com.sap.sse.gwt.client.mvp.example.hello.HelloViewImpl;

public class AppClientFactoryImpl extends ClientFactoryImpl implements AppClientFactory {
    private HelloView helloView;
    private GoodbyeView goodbyeView;
    
    protected AppClientFactoryImpl() {
        super(new TopLevelView() {
            private SimplePanel root = new SimplePanel();
            
            @Override
            public AcceptsOneWidget getContent() {
                return root;
            }

            @Override
            public Widget asWidget() {
                return root;
            }

            @Override
            public ErrorReporter getErrorReporter() {
                return new DefaultErrorReporter<TextMessages>(TextMessages.INSTANCE);
            }

        });
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

    @Override
    public Place getDefaultPlace() {
        return new HelloPlace("World!");
    }
}
