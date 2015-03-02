package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabsView;

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

        initWidget(ourUiBinder.createAndBindUi(this));

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
    public void setPresenter(SeriesTabsView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
}