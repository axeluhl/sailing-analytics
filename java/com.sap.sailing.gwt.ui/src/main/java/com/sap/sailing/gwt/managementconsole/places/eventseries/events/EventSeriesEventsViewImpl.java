package com.sap.sailing.gwt.managementconsole.places.eventseries.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EventSeriesEventsViewImpl extends Composite implements EventSeriesEventsView {

    interface EventSeriesEventsViewUiBinder extends UiBinder<Widget, EventSeriesEventsViewImpl> {
    }

    private static EventSeriesEventsViewUiBinder uiBinder = GWT.create(EventSeriesEventsViewUiBinder.class);

    @UiField
    EventSeriesEventsResources local_res;

    public EventSeriesEventsViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
    }

    @Override
    public void setPresenter(final Presenter presenter) {
    }

}