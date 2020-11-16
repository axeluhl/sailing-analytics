package com.sap.sailing.gwt.managementconsole.places.event.overview;

import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.event.overview.partials.EventCard;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventOverviewViewImpl extends Composite implements EventOverviewView {

    Logger logger = Logger.getLogger(this.getClass().getName());
    
    private Presenter presenter;

    interface EventOverviewViewImplUiBinder extends UiBinder<Widget, EventOverviewViewImpl> {
    }

    private static EventOverviewViewImplUiBinder uiBinder = GWT.create(EventOverviewViewImplUiBinder.class);

    @UiField
    EventOverviewResources local_res;

    @UiField
    FlowPanel cards;

    public EventOverviewViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
    }

    @Override
    public void renderEvents(List<EventDTO> events) {
        cards.clear();
        for (EventDTO event : events) {
            EventCard card = new EventCard(event, presenter);
            cards.add(card);
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

}
