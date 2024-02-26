package com.sap.sailing.gwt.managementconsole.places.eventseries.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.eventseries.events.EventSeriesEventsPlace;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.EventSeriesOverviewPlace;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

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
        public void createEventSeries(String name, String description, String shortName, 
                boolean isPublic, String baseUrlAsString, ScoringSchemeType scoringSchemeType, 
                int[]discardThresholds) {
            getClientFactory().getEventService().createEventSeries(name, description, shortName, isPublic, baseUrlAsString, scoringSchemeType, 
                    discardThresholds, getCreateEventSeriesCallback());
        }
        
        private AsyncCallback<LeaderboardGroupDTO> getCreateEventSeriesCallback () {
            return new AsyncCallback<LeaderboardGroupDTO>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Error occured", caught);
                    getClientFactory().getPlaceController().goTo(new EventSeriesOverviewPlace());
                }

                @Override
                public void onSuccess(LeaderboardGroupDTO result) {
                    getClientFactory().getPlaceController().goTo(new EventSeriesEventsPlace(result.getId()));
                }
            };
        }
        
        @Override
        public void cancelCreateEventSeries() {
            getClientFactory().getPlaceController().goTo(new EventSeriesOverviewPlace());
        }
        
    }
    
}
