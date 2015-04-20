package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView;

public class SeriesOverviewTabView extends Composite implements SeriesTabView<SeriesOverviewPlace> {

    interface MyBinder extends UiBinder<Widget, SeriesOverviewTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private SeriesView.Presenter currentPresenter;

    public SeriesOverviewTabView() {
    }

    @Override
    public Class<SeriesOverviewPlace> getPlaceClassForActivation() {
        return SeriesOverviewPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.NOT_AVAILABLE_SHOW_NEXT_AVAILABLE;
    }

    @Override
    public void start(SeriesOverviewPlace myPlace, AcceptsOneWidget contentArea) {
        initWidget(ourUiBinder.createAndBindUi(this));
        
        // TODO implement contents
        
        contentArea.setWidget(this);
        
    }

    @Override
    public void stop() {

    }

    @Override
    public SeriesOverviewPlace placeToFire() {
        return new SeriesOverviewPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(SeriesView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
}