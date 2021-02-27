package com.sap.sailing.gwt.managementconsole.places.event.create;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;

public class CreateEventActivity extends AbstractManagementConsoleActivity<CreateEventPlace> {

    public CreateEventActivity(final ManagementConsoleClientFactory clientFactory, final CreateEventPlace place) {
        super(clientFactory, place);
    }

    private CreateEventView createEventView;
    
    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        createEventView = new CreateEventViewImpl();
        CreateEventViewPresenter createEventViewPresenter = new CreateEventViewPresenter(createEventView);
        container.setWidget(createEventView);
    }

    private class CreateEventViewPresenter implements CreateEventView.Presenter {

        public CreateEventViewPresenter(CreateEventView createEventView) {
            createEventView.setPresenter(this);
        }
        
        @Override
        public void createEvent() {
            getClientFactory().getPlaceController().goTo(new EventOverviewPlace());
        }
        @Override
        public void cancelCreateEvent() {
            getClientFactory().getPlaceController().goTo(new EventOverviewPlace());
        }
        
    }
    
}
