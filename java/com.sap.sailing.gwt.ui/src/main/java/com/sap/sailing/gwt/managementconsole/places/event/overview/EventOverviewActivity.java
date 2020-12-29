package com.sap.sailing.gwt.managementconsole.places.event.overview;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.events.EventListResponseEvent;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.event.media.EventMediaPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventOverviewActivity extends AbstractManagementConsoleActivity<EventOverviewPlace>
        implements EventOverviewView.Presenter {

    public EventOverviewActivity(final ManagementConsoleClientFactory clientFactory, final EventOverviewPlace place) {
        super(clientFactory, place);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        final EventOverviewView view = getClientFactory().getViewFactory().getEventOverviewView();
        view.setPresenter(this);
        container.setWidget(view);
        // add refresh event listener in case of response from request event.
        eventBus.addHandler(EventListResponseEvent.TYPE,
                (final EventListResponseEvent event) -> view.renderEvents(event.getEvents()));
        getClientFactory().getEventService().requestEventList(/* forceRequestFromService */ false);
    }

    @Override
    public void reloadEventList() {
        getClientFactory().getEventService().requestEventList(/* forceRequestFromService */ true);
    }

    @Override
    public void navigateToEvent(final EventDTO event) {
        getClientFactory().getPlaceController().goTo(new EventMediaPlace(event.id));
    }

}
