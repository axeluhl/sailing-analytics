package com.sap.sailing.gwt.autoplay.client.places.startclassic;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;

public interface StartClassicView {

    public interface Presenter {
    }

    void startingWith(Presenter p, AcceptsOneWidget panel);

    void setImage(String string);

    void showFailure(FailureEvent failureEvent, Command onContinue, Command onReset);
}
