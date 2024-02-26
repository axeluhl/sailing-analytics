package com.sap.sailing.gwt.managementconsole.places.event.overview.partials;

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
import com.sap.sailing.gwt.common.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.managementconsole.places.event.EventPresenter;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewResources;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public class EventCard extends Composite {

    interface EventCardUiBinder extends UiBinder<Widget, EventCard> {
    }

    private static EventCardUiBinder uiBinder = GWT.create(EventCardUiBinder.class);

    @UiField
    EventOverviewResources local_res;

    @UiField
    ManagementConsoleResources app_res;

    @UiField
    Element card, container;

    @UiField(provided = true)
    EventInfo info;

    private final Runnable openContextMenu;

    public EventCard(final EventMetadataDTO event, final EventPresenter presenter) {
        info = new EventInfo(event);
        initWidget(uiBinder.createAndBindUi(this));

        ofNullable(event.getThumbnailImageURL())
                .ifPresent(imageUrl -> this.card.getStyle().setBackgroundImage("url(' " + imageUrl + "')"));

        Event.sinkEvents(card, Event.ONCLICK);
        Event.setEventListener(card, e -> presenter.navigateToEvent(event));

        openContextMenu = () -> presenter.requestContextMenu(event);
    }

    @UiHandler("contextMenu")
    void onContextMenuIconClick(final ClickEvent event) {
        openContextMenu.run();
        event.stopPropagation();
        event.preventDefault();
    }

}
