package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;

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
        return currentPresenter.getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA ? TabView.State.NOT_AVAILABLE_SHOW_NEXT_AVAILABLE
                : TabView.State.VISIBLE;
    }

    @Override
    public void start(RegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {
        racesListLive = new RacesListLive(currentPresenter, false);
        stage = new EventOverviewStage(currentPresenter);

        initWidget(ourUiBinder.createAndBindUi(this));
        
        // TODO CF
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());

        // TODO regatta specific depending on event type
        refreshManager.add(stage, new GetEventOverviewStageAction(currentPresenter.getCtx().getEventDTO().getId()));
        refreshManager.add(racesListLive, new GetLiveRacesForRegattaAction(currentPresenter.getCtx().getEventDTO()
                .getId(), currentPresenter.getCtx().getRegattaId()));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaOverviewTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;
    
    @UiField(provided = true)
    RacesListLive racesListLive;
    @UiField(provided = true) EventOverviewStage stage;

    @Override
    public RegattaOverviewPlace placeToFire() {
        return new RegattaOverviewPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }

}
