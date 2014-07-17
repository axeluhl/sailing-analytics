package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaRacesRaceday extends Composite {
    private static EventRegattaRacesRacedayUiBinder uiBinder = GWT.create(EventRegattaRacesRacedayUiBinder.class);

    interface EventRegattaRacesRacedayUiBinder extends UiBinder<Widget, EventRegattaRacesRaceday> {
    }

    @UiField SpanElement raceDayDate;
    @UiField HTMLPanel racesPanel;
    
    // TODO will be used soon:
//    private final Timer timerForClientServerOffset;
//    private final EventPageNavigator pageNavigator;

    public EventRegattaRacesRaceday(RaceGroupSeriesDTO series, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
//        this.timerForClientServerOffset = timerForClientServerOffset;
//        this.pageNavigator = pageNavigator;
        
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
}
