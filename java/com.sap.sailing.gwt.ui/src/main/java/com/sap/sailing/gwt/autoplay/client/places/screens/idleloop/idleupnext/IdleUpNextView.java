package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.common.Util.Pair;

public interface IdleUpNextView {
    void startingWith(IdleUpNextPresenter p, AcceptsOneWidget panel);

    void setBackgroudImage(String string);

    public interface IdleUpNextPresenter {
    }

    void setData(ArrayList<Pair<RegattaAndRaceIdentifier, Date>> data);
}
