package com.sap.sailing.gwt.home.mobile.places.event.races;

import java.util.Optional;

import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;

public interface RacesView extends EventViewBase {

    public interface Presenter extends EventViewBase.Presenter {

        Optional<String> getPreferredSeriesName();
    }

}
