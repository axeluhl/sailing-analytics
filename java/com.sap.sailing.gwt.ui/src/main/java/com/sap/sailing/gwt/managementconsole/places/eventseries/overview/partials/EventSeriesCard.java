package com.sap.sailing.gwt.managementconsole.places.eventseries.overview.partials;

import static java.util.Optional.ofNullable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.EventSeriesOverviewResources;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.EventSeriesOverviewView.Presenter;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public class EventSeriesCard extends Composite {

    interface EventSeriesCardUiBinder extends UiBinder<Widget, EventSeriesCard> {
    }

    private static EventSeriesCardUiBinder uiBinder = GWT.create(EventSeriesCardUiBinder.class);

    @UiField
    EventSeriesOverviewResources local_res;

    @UiField
    ManagementConsoleResources app_res;

    @UiField
    Element card, container;

    @UiField(provided = true)
    EventSeriesInfo info;

    private final Runnable openContextMenu;

    public EventSeriesCard(final EventSeriesMetadataDTO eventSeries, final Presenter presenter) {
        info = new EventSeriesInfo(eventSeries);
        initWidget(uiBinder.createAndBindUi(this));

        ofNullable(eventSeries.getThumbnailImageURL())
                .ifPresent(imageUrl -> this.card.getStyle().setBackgroundImage("url(' " + imageUrl + "')"));

        Event.sinkEvents(card, Event.ONCLICK);
        Event.setEventListener(card, e -> presenter.navigateToEventSeries(eventSeries));

        openContextMenu = () -> presenter.requestContextMenu(eventSeries);
    }

    @UiHandler("contextMenu")
    void onContextMenuIconClick(final ClickEvent event) {
        openContextMenu.run();
        event.stopPropagation();
        event.preventDefault();
    }

}
