package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.events.recent.RecentEventTeaser;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class SeriesEventsTabView extends Composite implements SeriesTabView<SeriesEventsPlace> {

    interface MyBinder extends UiBinder<Widget, SeriesEventsTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private SeriesView.Presenter currentPresenter;

    @UiField FlowPanel eventsContainer;
    
    public SeriesEventsTabView() {
    }

    @Override
    public Class<SeriesEventsPlace> getPlaceClassForActivation() {
        return SeriesEventsPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(SeriesEventsPlace myPlace, AcceptsOneWidget contentArea) {
        initWidget(ourUiBinder.createAndBindUi(this));

        for (EventMetadataDTO eventOfSeries : currentPresenter.getCtx().getSeriesDTO().getEvents()) {
            final PlaceNavigation<EventDefaultPlace> eventNavigation = currentPresenter.getEventNavigation(eventOfSeries.getId());
            RecentEventTeaser eventTeaser = new RecentEventTeaser(eventNavigation, eventOfSeries);
            eventsContainer.add(eventTeaser);
        }
        
        contentArea.setWidget(this);
        
    }

    @Override
    public void stop() {

    }

    @Override
    public SeriesEventsPlace placeToFire() {
        return new SeriesEventsPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(SeriesView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
}