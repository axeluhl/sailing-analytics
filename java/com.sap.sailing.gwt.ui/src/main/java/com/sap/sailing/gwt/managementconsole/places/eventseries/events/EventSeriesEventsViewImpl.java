package com.sap.sailing.gwt.managementconsole.places.eventseries.events;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.partials.contextmenu.ContextMenu;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewResources;
import com.sap.sailing.gwt.managementconsole.places.event.overview.partials.EventCard;
import com.sap.sailing.gwt.managementconsole.places.event.overview.partials.EventInfo;
import com.sap.sailing.gwt.managementconsole.places.eventseries.events.partials.EventSeriesHeader;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.partials.EventSeriesInfo;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventSeriesEventsViewImpl extends Composite implements EventSeriesEventsView {

    interface EventSeriesEventsViewUiBinder extends UiBinder<Widget, EventSeriesEventsViewImpl> {
    }

    private static EventSeriesEventsViewUiBinder uiBinder = GWT.create(EventSeriesEventsViewUiBinder.class);

    @UiField
    EventSeriesEventsResources local_res;

    @UiField
    EventOverviewResources event_res;

    @UiField
    ManagementConsoleResources app_res;

    @UiField
    StringMessages i18n;

    @UiField
    Element headerContainer;

    @UiField
    ScrollPanel scrollContainer;

    @UiField
    FlowPanel cards;

    @UiField
    Anchor addEventAnchor, filterEventAnchor, searchEventAnchor;
    
    @UiField
    TextBox eventSeriesDescription;
    
    @UiField
    TextBox eventSeriesName;
    
    @UiField
    EventSeriesHeader eventSeriesHeader;
    
    private EventSeriesMetadataDTO eventSeries;
    
    private Presenter presenter;

    public EventSeriesEventsViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
        eventSeries = new EventSeriesMetadataDTO("", null);
        eventSeriesName.setValue("Test");
        eventSeriesDescription.setValue("Test Description");
        eventSeriesHeader.setEventName("Test Event Series");
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void renderEvents(final List<EventMetadataDTO> events) {
        cards.clear();
        events.stream().map(event -> new EventCard(event, presenter)).forEach(cards::add);
    }

    @Override
    public void showContextMenu(final EventMetadataDTO event) {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setHeaderWidget(new EventInfo(event));
        contextMenu.show();
    }

    @Override
    public void showContextMenu(final EventSeriesMetadataDTO eventSeries) {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setHeaderWidget(new EventSeriesInfo(eventSeries));
        contextMenu.show();
    }
    
    public EventSeriesMetadataDTO getEventSeries() {
        return eventSeries;
    }

}