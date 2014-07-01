package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class EventRegattaSchedule extends Composite {
    private static EventRegattaScheduleUiBinder uiBinder = GWT.create(EventRegattaScheduleUiBinder.class);

    interface EventRegattaScheduleUiBinder extends UiBinder<Widget, EventRegattaSchedule> {
    }

    @UiField SpanElement regattaName;
    
    @SuppressWarnings("unused")
    private final EventDTO event;
    
    public EventRegattaSchedule(EventDTO event, EventPageNavigator pageNavigator) {
        this.event = event;
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setRacesFromRegatta(RegattaDTO regatta) {
        regattaName.setInnerHTML(regatta.getName());
    }

    public void setRacesFromFlexibleLeaderboard(StrippedLeaderboardDTO flexibleLeaderboard) {
        
    }

}
