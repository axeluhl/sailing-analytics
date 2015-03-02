package com.sap.sailing.gwt.home.client.place.event2.partials.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;

/**
 * Not used yet
 * @author Frank
 *
 */
public class EventRegattaRacesRaceday extends Composite {
    private static EventRegattaRacesRacedayUiBinder uiBinder = GWT.create(EventRegattaRacesRacedayUiBinder.class);

    interface EventRegattaRacesRacedayUiBinder extends UiBinder<Widget, EventRegattaRacesRaceday> {
    }

    @UiField DivElement raceDayDate;
    @UiField HTMLPanel racesPanel;

    public EventRegattaRacesRaceday(RaceGroupSeriesDTO series) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        raceDayDate.setInnerText("A date to fill in here");
    }
}
