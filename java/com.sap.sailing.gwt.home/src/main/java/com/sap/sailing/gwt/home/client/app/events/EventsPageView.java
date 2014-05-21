package com.sap.sailing.gwt.home.client.app.events;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.sap.sailing.gwt.home.client.app.event.EventParameterTokens;
import com.sap.sailing.gwt.home.client.shared.PageNameConstants;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

public class EventsPageView extends Composite implements EventsPagePresenter.MyView {
    private static EventsPageViewUiBinder uiBinder = GWT.create(EventsPageViewUiBinder.class);

    interface EventsPageViewUiBinder extends UiBinder<Widget, EventsPageView> {
    }

    @UiField
    TextBox queryInput;
    @UiField
    Button searchButton;
    @UiField
    FlowPanel eventListPanel;

    @UiField(provided = true)
    EventsTable eventsTable;

    private final PlaceManager placeManager;

    @Inject
    public EventsPageView(PlaceManager placeManager) {
        super();

        this.placeManager = placeManager;

        eventsTable = new EventsTable();

        initWidget(uiBinder.createAndBindUi(this));
        queryInput.getElement().setId("queryInput");
    }

    @UiHandler("searchButton")
    void buttonClick(ClickEvent event) {
    }

    @Override
    public void addToSlot(Object slot, IsWidget content) {
    }

    @Override
    public void removeFromSlot(Object slot, IsWidget content) {
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
    }

    @Override
    public void setEvents(List<EventDTO> events) {
        eventListPanel.clear();
        for (EventDTO event : events) {
            PlaceRequest request = new PlaceRequest.Builder().nameToken(PageNameConstants.eventPage)
                    .with(EventParameterTokens.TOKEN_ID, event.uuid.toString()).build();
            eventListPanel.add(new Hyperlink(event.getName(), placeManager.buildRelativeHistoryToken(request)));
        }

        eventsTable.setEvents(events);
    }

}
