package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaScheduleSeries extends Composite {
    private static EventRegattaScheduleSeriesUiBinder uiBinder = GWT.create(EventRegattaScheduleSeriesUiBinder.class);

    interface EventRegattaScheduleSeriesUiBinder extends UiBinder<Widget, EventRegattaScheduleSeries> {
    }

    @UiField SpanElement seriesName;
    @UiField SpanElement seriesDate;
    @UiField HTMLPanel fleetsListPanel;
    
    public EventRegattaScheduleSeries(StrippedLeaderboardDTO leaderboard, SeriesDTO series, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        seriesName.setInnerText(series.getName());
        seriesDate.setInnerText("A Date of the series???");

        for(FleetDTO fleet: series.getFleets()) {
            EventRegattaScheduleFleet eventRegattaScheduleFleet = new EventRegattaScheduleFleet(leaderboard, series, fleet, timerForClientServerOffset, pageNavigator); 
            fleetsListPanel.add(eventRegattaScheduleFleet);
        }
    }
    
}
