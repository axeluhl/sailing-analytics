package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabsView;
import com.sap.sailing.gwt.home.client.place.fakeseries.partials.header.SeriesHeaderResources;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesEventDTO;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class SeriesEventsTabView extends Composite implements SeriesTabView<SeriesEventsPlace> {

    interface MyBinder extends UiBinder<HTMLPanel, SeriesEventsTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private SeriesTabsView.Presenter currentPresenter;

    public SeriesEventsTabView() {
    }

    @Override
    public Class<SeriesEventsPlace> getPlaceClassForActivation() {
        return SeriesEventsPlace.class;
    }

    @Override
    public void start(SeriesEventsPlace myPlace, AcceptsOneWidget contentArea) {

//        initWidget(ourUiBinder.createAndBindUi(this));
//
//        contentArea.setWidget(this);
        
        // FIXME Temporary hack
        FlowPanel panel = new FlowPanel();
        for (EventSeriesEventDTO eventOfSeries : currentPresenter.getCtx().getSeriesDTO().getEvents()) {
            Anchor eventAnchor = new Anchor(eventOfSeries.getDisplayName());
            eventAnchor.getElement().getStyle().setDisplay(Display.BLOCK);
            eventAnchor.getElement().getStyle().setMarginTop(0.5, Unit.EM);
            eventAnchor.addStyleName(SeriesHeaderResources.INSTANCE.css().eventheader_intro_details_itemlink());
            final PlaceNavigation<EventDefaultPlace> eventNavigation = currentPresenter.getEventNavigation(eventOfSeries.getId());
            eventAnchor.setHref(eventNavigation.getTargetUrl());
            panel.add(eventAnchor);
            eventAnchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    event.preventDefault();
                    eventNavigation.goToPlace();
                }
            });
        }
        contentArea.setWidget(panel);
    }

    @Override
    public void stop() {

    }

    @Override
    public SeriesEventsPlace placeToFire() {
        return new SeriesEventsPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(SeriesTabsView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
}