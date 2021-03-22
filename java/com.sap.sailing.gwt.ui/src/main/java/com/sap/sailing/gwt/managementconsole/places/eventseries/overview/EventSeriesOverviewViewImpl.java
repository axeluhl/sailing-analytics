package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;

public class EventSeriesOverviewViewImpl extends Composite implements EventSeriesOverviewView {

    interface EventSeriesOverviewViewUiBinder extends UiBinder<Widget, EventSeriesOverviewViewImpl> {
    }

    private static EventSeriesOverviewViewUiBinder uiBinder = GWT.create(EventSeriesOverviewViewUiBinder.class);

    @UiField
    EventSeriesOverviewResources local_res;

    public EventSeriesOverviewViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
    }

    @Override
    public void setPresenter(final Presenter presenter) {
    }

    @Override
    public void renderEventSeries(final List<EventSeriesMetadataDTO> eventSeries) {
        final StringBuilder builder = new StringBuilder();
        eventSeries.forEach(series -> builder.append("<br/>")
                .append(series.getSeriesLeaderboardGroupId())
                .append(" - ").append(series.getSeriesDisplayName())
                .append(" - No. of Events: ").append(series.getEventsCount()));

        getElement().setInnerHTML(builder.toString());
    }

}