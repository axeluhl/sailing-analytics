package com.sap.sailing.gwt.managementconsole.places.dashboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DashboardViewImpl extends Composite implements DashboardView {

    interface DashboardViewUiBinder extends UiBinder<Widget, DashboardViewImpl> {
    }

    private static DashboardViewUiBinder uiBinder = GWT.create(DashboardViewUiBinder.class);

    @UiField
    DashboardResources local_res;

    @UiField
    Element seriesCard, eventCard;

    private Presenter presenter;

    public DashboardViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();

        Event.sinkEvents(seriesCard, Event.ONCLICK);
        Event.setEventListener(seriesCard, event -> presenter.navigateToEventSeries());

        Event.sinkEvents(eventCard, Event.ONCLICK);
        Event.setEventListener(eventCard, event -> presenter.navigateToEvents());
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("createSeries")
    void onCreateEventSeriesClicked(final ClickEvent event) {
        createClicked(event, presenter::navigateToCreateEventSeries);
    }

    @UiHandler("createEvent")
    void onCreateEventClicked(final ClickEvent event) {
        createClicked(event, presenter::navigateToCreateEvent);
    }

    private void createClicked(final ClickEvent event, final Runnable presenterFunction) {
        presenterFunction.run();
        event.stopPropagation();
        event.preventDefault();
    }

}