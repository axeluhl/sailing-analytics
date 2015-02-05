package com.sap.sailing.gwt.home.client.place.event2.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.BreadcrumbPane;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventHeader extends Composite {
    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    interface EventHeaderUiBinder extends UiBinder<Widget, EventHeader> {
    }
    
    @UiField
    BreadcrumbPane breadcrumbs;

    private final HomePlacesNavigator placeNavigator;
    private PlaceNavigation<SeriesPlace> seriesAnalyticsNavigation = null; 
    private PlaceNavigation<EventPlace> regattasNavigation = null;
    
    public EventHeader() {
        this(null, null, null);
    }
    
    public EventHeader(EventDTO event, HomePlacesNavigator placeNavigator, EventPlaceNavigator pageNavigator) {
//        super(event, pageNavigator);
        
        this.placeNavigator = placeNavigator;
        
        EventHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
