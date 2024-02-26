package com.sap.sailing.gwt.managementconsole.places.dashboard;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.event.create.CreateEventPlace;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.eventseries.create.CreateEventSeriesPlace;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.EventSeriesOverviewPlace;

public class DashboardActivity extends AbstractManagementConsoleActivity<DashboardPlace>
        implements DashboardView.Presenter {


    public DashboardActivity(final ManagementConsoleClientFactory clientFactory, final DashboardPlace place) {
        super(clientFactory, place);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        final DashboardView view = getClientFactory().getViewFactory().getDashboardView();
        view.setPresenter(this);
        container.setWidget(view);
    }

    @Override
    public void navigateToEventSeries() {
        getClientFactory().getPlaceController().goTo(new EventSeriesOverviewPlace());
    }

    @Override
    public void navigateToCreateEventSeries() {
        getClientFactory().getPlaceController().goTo(new CreateEventSeriesPlace());
    }

    @Override
    public void navigateToEvents() {
        getClientFactory().getPlaceController().goTo(new EventOverviewPlace());
    }

    @Override
    public void navigateToCreateEvent() {
        getClientFactory().getPlaceController().goTo(new CreateEventPlace());

    }

}
