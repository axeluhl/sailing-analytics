package com.sap.sailing.gwt.managementconsole.places.regatta.create;

import java.util.UUID;
import java.util.logging.Logger;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class AddRegattaActivity extends AbstractManagementConsoleActivity<AddRegattaPlace> {

    private static final Logger LOG = Logger.getLogger(AddRegattaActivity.class.getName());
    
    private final UUID eventId;
    private AddRegattaView addRegattaView;
    
    public AddRegattaActivity(final ManagementConsoleClientFactory clientFactory, final AddRegattaPlace place) {
        super(clientFactory, place);
        this.eventId = place.getEventId();
    }
    
    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        addRegattaView = new AddRegattaViewImpl();
        new AddRegattaViewPresenter(addRegattaView);
        container.setWidget(addRegattaView);
    }
   
    private class AddRegattaViewPresenter implements AddRegattaView.Presenter {

        public AddRegattaViewPresenter(AddRegattaView addRegattaView) {
            addRegattaView.setPresenter(this);
        }

        @Override
        public boolean validateRegattaName(String regattaName) {
            for(RegattaDTO oldRegatta : getClientFactory().getRegattaService().getRegattas()) {
                if (oldRegatta.getName().equals(regattaName)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public void cancelAddRegatta() {
            getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(eventId));
        }     
        
        @Override
        public void addRegatta(String regattaName, String boatClassName, final RankingMetrics ranking, final Integer numberOfRaces, final ScoringSchemeType scoringSystem) {  
            getClientFactory().getRegattaService().addRegatta(eventId, regattaName, boatClassName, ranking, numberOfRaces, scoringSystem, new AsyncCallback<RegattaDTO>() {
                @Override
                public final void onFailure(Throwable t) {
                    LOG.severe("addRegatta :: Cannot load add regatta");
                    getClientFactory().getErrorReporter().reportError("Error", "Cannot add regatta");
                }
                
                @Override
                public void onSuccess(RegattaDTO regatta) { 
                    getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(eventId));                   
                }  
            });
        }
    }
    
}
