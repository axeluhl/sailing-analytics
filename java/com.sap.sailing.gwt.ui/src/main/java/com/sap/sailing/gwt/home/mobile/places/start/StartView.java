package com.sap.sailing.gwt.home.mobile.places.start;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.start.EventQuickfinderDTO;
import com.sap.sailing.gwt.ui.shared.general.EventLinkAndMetadataDTO;

public interface StartView {

    Widget asWidget();
    
    void setQuickFinderValues(Collection<EventQuickfinderDTO> events);
    
    public interface Presenter {
        MobilePlacesNavigator getNavigator();
        
        PlaceNavigation<?> getEventNavigation(EventQuickfinderDTO event);
    }

    void setFeaturedEvents(List<? extends EventLinkAndMetadataDTO> list);
}

