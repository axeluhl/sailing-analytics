package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.client.shared.dispatch.AutomaticBatchingDispatch;
import com.sap.sailing.gwt.home.client.shared.dispatch.SimpleDispatch;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForEventAction;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaOverviewTabView extends Composite implements MultiregattaTabView<MultiregattaOverviewPlace> {

    private Presenter currentPresenter;

    public MultiregattaOverviewTabView() {

    }

    @Override
    public Class<MultiregattaOverviewPlace> getPlaceClassForActivation() {
        return MultiregattaOverviewPlace.class;
    }
    
    @Override
    public void setPresenter(EventMultiregattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(MultiregattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {
        racesListLive = new RacesListLive(currentPresenter);

        initWidget(ourUiBinder.createAndBindUi(this));
        
        // TODO CF
        RefreshManager refreshManager = new RefreshManager(this, new AutomaticBatchingDispatch(new SimpleDispatch(null)));
        
        refreshManager.add(racesListLive, new GetLiveRacesForEventAction(currentPresenter.getCtx().getEventDTO().getId()));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaOverviewTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    
    @UiField(provided = true) RacesListLive racesListLive;

    @Override
    public MultiregattaOverviewPlace placeToFire() {
        return new MultiregattaOverviewPlace(currentPresenter.getCtx());
    }

}