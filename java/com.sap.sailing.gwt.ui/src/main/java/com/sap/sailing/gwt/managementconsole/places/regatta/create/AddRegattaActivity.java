package com.sap.sailing.gwt.managementconsole.places.regatta.create;

import java.util.UUID;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;

public class AddRegattaActivity extends AbstractManagementConsoleActivity<AddRegattaPlace> {

    private final UUID eventId;
    private AddRegattaView addRegattaView;
    
    public AddRegattaActivity(final ManagementConsoleClientFactory clientFactory, final AddRegattaPlace place) {
        super(clientFactory, place);
        this.eventId = place.getEventId();
    }
    
    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        addRegattaView = new AddRegattaViewImpl();
        AddRegattaViewPresenter addRegattaViewPresenter = new AddRegattaViewPresenter(addRegattaView);
        container.setWidget(addRegattaView);
    }
   
    private class AddRegattaViewPresenter implements AddRegattaView.Presenter {

        public AddRegattaViewPresenter(AddRegattaView addRegattaView) {
            addRegattaView.setPresenter(this);
        }

        @Override
        public void addRegatta() {
            getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(eventId));
        }
        
        @Override
        public void cancelAddRegatta() {
            getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(eventId));
        }
        
    }
    
}
