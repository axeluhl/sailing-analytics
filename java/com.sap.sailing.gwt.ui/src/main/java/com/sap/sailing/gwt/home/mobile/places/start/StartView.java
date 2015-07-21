package com.sap.sailing.gwt.home.mobile.places.start;

import java.util.Collection;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.start.StartViewDTO;

public interface StartView {

    Widget asWidget();
    
    void setQuickFinderValues(Collection<EventReferenceDTO> events);
    
    public interface Presenter {
        MobilePlacesNavigator getNavigator();
    }

    void setData(StartViewDTO data);
}

