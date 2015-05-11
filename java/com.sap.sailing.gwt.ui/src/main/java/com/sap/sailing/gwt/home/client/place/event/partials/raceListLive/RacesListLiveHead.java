package com.sap.sailing.gwt.home.client.place.event.partials.raceListLive;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RacesListLiveHead extends Composite {

    private static RacesListLiveHeadUiBinder uiBinder = GWT.create(RacesListLiveHeadUiBinder.class);

    interface RacesListLiveHeadUiBinder extends UiBinder<Widget, RacesListLiveHead> {
    }

    public RacesListLiveHead() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
