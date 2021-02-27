package com.sap.sailing.gwt.managementconsole.places.event.overview;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.events.EventListResponseEvent;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.event.create.CreateEventPlace;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;
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
        eventBus.addHandler(EventListResponseEvent.TYPE, event -> view.renderEvents(event.getEvents()));
        getClientFactory().getEventService().requestEventList(/* forceRequestFromService */ false);
    }

    @Override
    public void reloadEventList() {
        getClientFactory().getEventService().requestEventList(/* forceRequestFromService */ true);
    }

    @Override
    public void navigateToEvent(final EventDTO event) {
        getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(event.id));
    }
    
    @Override
    public void navigateToCreateEvent() {
        getClientFactory().getPlaceController().goTo(new CreateEventPlace());
    }
    

}
