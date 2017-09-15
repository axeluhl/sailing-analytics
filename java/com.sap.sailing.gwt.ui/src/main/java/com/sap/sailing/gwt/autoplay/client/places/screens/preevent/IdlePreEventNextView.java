package com.sap.sailing.gwt.autoplay.client.places.screens.preevent;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.utils.Countdown.RemainingTime;

public interface IdlePreEventNextView {
    public interface IdlePreEventNextPresenter {
    }

    void startingWith(IdlePreEventNextPresenter p, AcceptsOneWidget panel);

    void setBackgroudImage(String string);

    void setStartIn(RemainingTime invalid);
}
