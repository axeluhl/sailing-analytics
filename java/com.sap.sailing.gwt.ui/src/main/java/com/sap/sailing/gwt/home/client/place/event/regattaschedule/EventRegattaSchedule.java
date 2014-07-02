package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class EventRegattaSchedule extends Composite {
    private static EventRegattaScheduleUiBinder uiBinder = GWT.create(EventRegattaScheduleUiBinder.class);

    interface EventRegattaScheduleUiBinder extends UiBinder<Widget, EventRegattaSchedule> {
    }

    @UiField SpanElement regattaName;
    @UiField HTMLPanel seriesListPanel;    
    
    @SuppressWarnings("unused")
    private final EventDTO event;
    
    public EventRegattaSchedule(EventDTO event, EventPageNavigator pageNavigator) {
        this.event = event;
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setRacesFromRegatta(RegattaDTO regatta) {
        regattaName.setInnerHTML(regatta.getName());
        
        for(SeriesDTO series: regatta.series) {
            EventRegattaScheduleSeries eventRegattaScheduleSeries = new EventRegattaScheduleSeries(regatta, series); 
            seriesListPanel.add(eventRegattaScheduleSeries);
        }
    }

    public void setRacesFromFlexibleLeaderboard(StrippedLeaderboardDTO flexibleLeaderboard) {
        
    }

}
