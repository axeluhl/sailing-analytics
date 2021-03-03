package com.sap.sailing.gwt.managementconsole.places.event.create;

import java.util.Date;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class CreateEventActivity extends AbstractManagementConsoleActivity<CreateEventPlace> {

    public CreateEventActivity(final ManagementConsoleClientFactory clientFactory, final CreateEventPlace place) {
        super(clientFactory, place);
    }

    private CreateEventView createEventView;
    
    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        createEventView = new CreateEventViewImpl();
        new CreateEventViewPresenter(createEventView);
        container.setWidget(createEventView);
    }

    private class CreateEventViewPresenter implements CreateEventView.Presenter {

        public CreateEventViewPresenter(CreateEventView createEventView) {
            createEventView.setPresenter(this);
        }
        
        @Override
        public void createEvent(String name, String venue, Date date, List<String> courseAreaNames) {
            getClientFactory().getEventService().createEvent(name, venue, date, courseAreaNames, getCreateEventCallback());
        }
        
        private AsyncCallback<EventDTO> getCreateEventCallback () {
            return new AsyncCallback<EventDTO>() {

                @Override
                public void onFailure(Throwable caught) {
                    getClientFactory().getPlaceController().goTo(new EventOverviewPlace());
                }

                @Override
                public void onSuccess(EventDTO result) {
                    getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(result.getId()));
                }
            };
        }
        
        @Override
        public void cancelCreateEvent() {
            getClientFactory().getPlaceController().goTo(new EventOverviewPlace());
        }
        
    }
    
}
