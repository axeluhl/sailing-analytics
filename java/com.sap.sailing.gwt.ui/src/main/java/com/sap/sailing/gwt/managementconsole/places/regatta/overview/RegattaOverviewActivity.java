package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import java.util.UUID;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.events.regatta.RegattaListResponseEvent;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaOverviewActivity extends AbstractManagementConsoleActivity<RegattaOverviewPlace>
        implements RegattaOverviewView.Presenter {

    UUID eventId;
    
    public RegattaOverviewActivity(final ManagementConsoleClientFactory clientFactory, final RegattaOverviewPlace place) {
        super(clientFactory, place);
        this.eventId = place.getEventId();
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        final RegattaOverviewView view = getClientFactory().getViewFactory().getRegattaOverviewView();
        view.setPresenter(this);
        container.setWidget(view);
        eventBus.addHandler(RegattaListResponseEvent.TYPE,
                (final RegattaListResponseEvent event) -> view.renderRegattas(event.getRegattas()));
        getClientFactory().getRegattaService().requestRegattaList(eventId, /* forceRequestFromService */ true);
    }

    @Override
    public void reloadRegattaList(UUID eventId) {
        getClientFactory().getRegattaService().requestRegattaList(eventId, /* forceRequestFromService */ true);
    }

    @Override
    public void navigateToRegatta(final RegattaDTO regatta) {
        //getClientFactory().getPlaceController().goTo(new RegattaPlace(regatta.id));
    }

}
