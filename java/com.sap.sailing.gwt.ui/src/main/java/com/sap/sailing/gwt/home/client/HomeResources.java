package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface HomeResources extends ClientBundle {
    public static final HomeResources INSTANCE = GWT.create(HomeResources.class);

    @Source("com/sap/sailing/gwt/home/main.css")
    MainCss mainCss();

    public interface MainCss extends CssResource {
        String grid();

        String column();

        String columns();

        String small1();

        String small2();

        String small3();

        String small4();

        String small5();

        String small6();

        String small7();

        String small8();

        String small9();

        String small10();

        String small11();

        String small12();

        String smalloffset0();

        String smalloffset1();

        String smalloffset2();

        String smalloffset3();

        String smalloffset4();

        String smalloffset5();

        String smalloffset6();

        String smalloffset7();

        String smalloffset8();

        String smalloffset9();

        String smalloffset10();

        String smalloffset11();

        String smallcentered();

        String smalluncentered();

        String button();

        String buttoninactive();

        String label();

        String labellive();

        String labeless();

        String labelworldcup();

        String labelbundesliga();

        String event();

        String event_image();

        String event_series();

        String event_name();

        String event_location();

        String dummy();
    }
}