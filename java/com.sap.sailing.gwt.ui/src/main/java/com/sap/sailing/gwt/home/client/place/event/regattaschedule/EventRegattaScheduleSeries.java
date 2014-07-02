package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class EventRegattaScheduleSeries extends Composite {
    private static EventRegattaScheduleSeriesUiBinder uiBinder = GWT.create(EventRegattaScheduleSeriesUiBinder.class);

    interface EventRegattaScheduleSeriesUiBinder extends UiBinder<Widget, EventRegattaScheduleSeries> {
    }

    @SuppressWarnings("unused")
    private final RegattaDTO regatta;
    
    @UiField SpanElement seriesName;
    @UiField SpanElement seriesDate;
    @UiField HTMLPanel fleetsListPanel;
    
    public EventRegattaScheduleSeries(RegattaDTO regatta, SeriesDTO series) {
        this.regatta = regatta;
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        seriesName.setInnerText(series.getName());
        seriesDate.setInnerText("A Date of the series???");

        for(FleetDTO fleet: series.getFleets()) {
            EventRegattaScheduleFleet eventRegattaScheduleFleet = new EventRegattaScheduleFleet(regatta, series, fleet); 
            fleetsListPanel.add(eventRegattaScheduleFleet);
        }
    }
    
}
