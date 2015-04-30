package com.sap.sailing.gwt.home.client.place.event.partials.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;

public class EventRegattaRacesNavigationLink extends Composite {
    private static EventRegattaRacesNavigationLinkUiBinder uiBinder = GWT.create(EventRegattaRacesNavigationLinkUiBinder.class);

    interface EventRegattaRacesNavigationLinkUiBinder extends UiBinder<Widget, EventRegattaRacesNavigationLink> {
    }

    @UiField SpanElement phaseName;
    
    public EventRegattaRacesNavigationLink(RaceGroupSeriesDTO series) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        phaseName.setInnerText(series.getName());
    }
    
}
