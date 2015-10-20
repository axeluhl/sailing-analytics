package com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.partials.eventstage.EventOverviewStage;
import com.sap.sailing.gwt.home.desktop.partials.liveraces.LiveRacesList;
import com.sap.sailing.gwt.home.desktop.partials.multiregattalist.MultiRegattaListItem;
import com.sap.sailing.gwt.home.desktop.partials.standings.StandingsList;
import com.sap.sailing.gwt.home.desktop.partials.statistics.StatisticsBox;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManagerWithErrorAndBusy;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaStatisticsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaWithProgressAction;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata.RegattaState;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaOverviewTabView extends Composite implements RegattaTabView<RegattaOverviewPlace> {

    interface MyBinder extends UiBinder<HTMLPanel, RegattaOverviewTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;
    
    @UiField SimplePanel regattaInfoContainerUi;
    @UiField(provided = true) LiveRacesList liveRacesListUi;
    @UiField(provided = true) EventOverviewStage stageUi;
    @UiField(provided = true) StandingsList standingsUi;
    @UiField(provided = true) StatisticsBox statisticsBoxUi;

    public RegattaOverviewTabView() {
    }

    @Override
    public Class<RegattaOverviewPlace> getPlaceClassForActivation() {
        return RegattaOverviewPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        if(ExperimentalFeatures.SHOW_SINGLE_REGATTA_OVERVIEW) {
            return currentPresenter.getEventDTO().getType() == EventType.MULTI_REGATTA ? TabView.State.NOT_AVAILABLE_SHOW_NEXT_AVAILABLE
                    : TabView.State.VISIBLE;
        } else {
            return TabView.State.NOT_AVAILABLE_SHOW_NEXT_AVAILABLE;
        }
    }

    @Override
    public void start(RegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {
        liveRacesListUi = new LiveRacesList(currentPresenter, false);
        stageUi = new EventOverviewStage(currentPresenter);
        statisticsBoxUi = new StatisticsBox(false);
        standingsUi = new StandingsList(currentPresenter.getRegattaMetadata().getState() == RegattaState.FINISHED, currentPresenter.getRegattaLeaderboardNavigation(currentPresenter.getRegattaId()));

        initWidget(ourUiBinder.createAndBindUi(this));
        
        RefreshManager refreshManager = new RefreshManagerWithErrorAndBusy(this, contentArea, currentPresenter.getDispatch(), currentPresenter.getErrorAndBusyClientFactory());
        refreshManager.add(new RefreshableWidget<RegattaWithProgressDTO>() {
            @Override
            public void setData(RegattaWithProgressDTO data) {
                regattaInfoContainerUi.setWidget(new MultiRegattaListItem(data, true));
            }
        }, new GetRegattaWithProgressAction(currentPresenter.getEventDTO().getId(), currentPresenter.getRegattaId()));

        stageUi.setupRefresh(refreshManager);
        refreshManager.add(liveRacesListUi.getRefreshable(), new GetLiveRacesForRegattaAction(currentPresenter.getEventDTO()
                .getId(), currentPresenter.getRegattaId()));
        refreshManager.add(standingsUi, new GetMiniLeaderbordAction(currentPresenter.getEventDTO().getId(), currentPresenter.getRegattaId(), 5));
        refreshManager.add(statisticsBoxUi, new GetRegattaStatisticsAction(currentPresenter.getEventDTO().getId(), currentPresenter.getRegattaId()));
    }

    @Override
    public void stop() {

    }

    @Override
    public RegattaOverviewPlace placeToFire() {
        return new RegattaOverviewPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }

}
