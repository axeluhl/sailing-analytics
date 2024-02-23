package com.sap.sailing.gwt.managementconsole.places.eventseries.create;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.EventSeriesOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class CreateEventSeriesActivity extends AbstractManagementConsoleActivity<CreateEventSeriesPlace> {

    public CreateEventSeriesActivity(final ManagementConsoleClientFactory clientFactory, final CreateEventSeriesPlace place) {
        super(clientFactory, place);
    }

    private CreateEventSeriesView createEventSeriesView;
    
    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        createEventSeriesView = new CreateEventSeriesViewImpl();
        new CreateEventSeriesViewPresenter(createEventSeriesView);
        container.setWidget(createEventSeriesView);
    }

    private class CreateEventSeriesViewPresenter implements CreateEventSeriesView.Presenter {

        public CreateEventSeriesViewPresenter(CreateEventSeriesView createEventSeriesView) {
            createEventSeriesView.setPresenter(this);
        }
        
        @Override
        public void createEventSeries(String name) {
            getClientFactory().getEventService().createEventSeries(name, getCreateEventSeriesCallback());
        }
        
        private AsyncCallback<EventDTO> getCreateEventSeriesCallback () {
            return new AsyncCallback<EventDTO>() {

                @Override
                public void onFailure(Throwable caught) {
                    getClientFactory().getPlaceController().goTo(new EventSeriesOverviewPlace());
                }

                @Override
                public void onSuccess(EventDTO result) {
                    getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(result.getId()));
                }
            };
        }
        
        @Override
        public void cancelCreateEventSeries() {
            getClientFactory().getPlaceController().goTo(new EventSeriesOverviewPlace());
        }
        
    }
    
}
