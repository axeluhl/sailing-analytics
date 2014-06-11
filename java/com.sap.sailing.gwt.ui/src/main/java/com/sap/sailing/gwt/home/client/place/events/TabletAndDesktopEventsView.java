package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class TabletAndDesktopEventsView extends Composite implements EventsView {
    private static EventsPageViewUiBinder uiBinder = GWT.create(EventsPageViewUiBinder.class);

    interface EventsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventsView> {
    }
    
    private final EventsActivity activity;

    @UiField
    TextBox queryInput;
    @UiField
    Button searchButton;
    @UiField
    FlowPanel eventListPanel;
    @UiField(provided=true)
    EventsTable eventsTable;

    public TabletAndDesktopEventsView(EventsActivity activity) {
        super();
        this.activity = activity;
        eventsTable = new EventsTable(activity);
        initWidget(uiBinder.createAndBindUi(this));
        queryInput.getElement().setId("queryInput");
    }
    
    public void setEvents(Iterable<EventDTO> events) {
        eventListPanel.clear();
        for (final EventDTO event : events) {
            Anchor request = new Anchor(event.getName());
            request.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent e) {
                    TabletAndDesktopEventsView.this.activity.goTo(new EventPlace(event.id.toString()));
                }
            });
            eventListPanel.add(request);
        }
        eventsTable.setEvents(events);
    }

    @UiHandler("searchButton")
    void buttonClick(ClickEvent event) {
    }
}
