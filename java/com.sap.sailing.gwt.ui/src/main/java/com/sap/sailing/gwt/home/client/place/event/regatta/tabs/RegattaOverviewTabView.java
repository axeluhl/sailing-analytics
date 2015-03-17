package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaOverviewTabView extends Composite implements RegattaTabView<RegattaOverviewPlace> {

    public RegattaOverviewTabView() {

    }

    @Override
    public Class<RegattaOverviewPlace> getPlaceClassForActivation() {
        return RegattaOverviewPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(RegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaOverviewTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;

    @Override
    public RegattaOverviewPlace placeToFire() {
        return new RegattaOverviewPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }

}