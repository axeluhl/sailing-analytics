package com.sap.sailing.gwt.managementconsole.places.event.overview;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.events.EventListResponseEvent;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.event.create.CreateEventPlace;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class EventOverviewActivity extends AbstractManagementConsoleActivity<EventOverviewPlace>
        implements EventOverviewView.Presenter {

    private final EventOverviewView view;

    public EventOverviewActivity(final ManagementConsoleClientFactory clientFactory, final EventOverviewPlace place) {
        super(clientFactory, place);
        this.view = getClientFactory().getViewFactory().getEventOverviewView();
        this.view.setPresenter(this);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        eventBus.addHandler(EventListResponseEvent.TYPE, events -> {
            view.renderEvents(events);
            container.setWidget(view);
        });
        getClientFactory().getEventService().requestEventList(/* forceRequestFromService */ false);
    }

    @Override
    public void reloadEventList() {
        getClientFactory().getEventService().requestEventList(/* forceRequestFromService */ true);
    }

    @Override
    public void requestContextMenu(final EventMetadataDTO event) {
        view.showContextMenu(event);
    }

    @Override
    public void navigateToEvent(final EventMetadataDTO event) {
        getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(event.getId()));
    }

    @Override
    public void navigateToCreateEvent() {
        getClientFactory().getPlaceController().goTo(new CreateEventPlace());
    }


    @Override
    public void advancedSettings(final EventMetadataDTO event) {
        Notification.notify("Settings for event: " + event.getId(), NotificationType.WARNING);
    }

    @Override
    public void deleteEvent(final EventMetadataDTO event) {
        Notification.notify("Deleting event: " + event.getId(), NotificationType.WARNING);
    }
}
