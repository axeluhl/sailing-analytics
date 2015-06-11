package com.sap.sailing.gwt.home.mobile.places.event;

import com.google.gwt.user.client.ui.Widget;

public interface EventView {

    Widget asWidget();

    void setSailorInfos(String description, String buttonLabel, String url);
    public interface Presenter {

    }
}

