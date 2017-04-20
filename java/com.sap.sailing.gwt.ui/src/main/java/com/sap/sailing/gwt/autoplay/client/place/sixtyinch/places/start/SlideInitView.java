package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;

public interface SlideInitView {
    void startingWith(SlideInitPresenter p, AcceptsOneWidget panel);
    public interface SlideInitPresenter {
    }

    void setImage(String string);

    void showFailure(FailureEvent failureEvent, Command onContinue, Command onReset);
}
