package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import java.util.UUID;
import java.util.logging.Logger;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.events.regatta.RegattaListResponseEvent;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaOverviewActivity extends AbstractManagementConsoleActivity<RegattaOverviewPlace>
        implements RegattaOverviewView.Presenter {

    private static final Logger LOG = Logger.getLogger(RegattaOverviewActivity.class.getName());

    private final UUID eventId;

    public RegattaOverviewActivity(final ManagementConsoleClientFactory clientFactory, final RegattaOverviewPlace place) {
        super(clientFactory, place);
        this.eventId = place.getEventId();
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        final RegattaOverviewView view = getClientFactory().getViewFactory().getRegattaOverviewView();
        view.setPresenter(this);
        container.setWidget(view);
        eventBus.addHandler(RegattaListResponseEvent.TYPE, event -> view.renderRegattas(event.getRegattas()));
        getClientFactory().getRegattaService().requestRegattaList(eventId, /* forceRequestFromService */ true);
        getClientFactory().getSailingService().getEventById(eventId, false, new AsyncCallback<EventDTO>() {

            @Override
            public void onSuccess(final EventDTO result) {
                view.renderEventName(result.getName());
                view.onResize();
            }

            @Override
            public void onFailure(final Throwable caught) {
                LOG.severe("requestEventList :: Cannot load events!");
                getClientFactory().getErrorReporter().reportError("Error", "Cannot load events!");

            }
        });
    }

    @Override
    public void reloadRegattaList(final UUID eventId) {
        getClientFactory().getRegattaService().requestRegattaList(eventId, /* forceRequestFromService */ true);
    }

    @Override
    public void navigateToEvents() {
        getClientFactory().getPlaceController().goTo(new EventOverviewPlace());
    }

    @Override
    public void navigateToRegatta(final RegattaDTO regatta) {
        //getClientFactory().getPlaceController().goTo(new RegattaPlace(regatta.id));
    }

}
