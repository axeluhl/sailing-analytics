package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

public interface SlideInitView {
    void startingWith(SlideInitPresenter p, AcceptsOneWidget panel);
    public interface SlideInitPresenter {
    }

    void setImage(String string);
}
