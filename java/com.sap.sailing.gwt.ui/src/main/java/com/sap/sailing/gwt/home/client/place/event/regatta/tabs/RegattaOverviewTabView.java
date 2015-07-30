package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListItem;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListStepsLegend;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.desktop.partials.standings.StandingsList;
import com.sap.sailing.gwt.home.desktop.partials.statistics.StatisticsBox;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventStatisticsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
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
    
    @UiField MultiRegattaListStepsLegend regattaProgressLegendUi;
    @UiField SimplePanel regattaInfoContainerUi;
    @UiField(provided = true)
    RacesListLive racesListLive;
    @UiField(provided = true) EventOverviewStage stage;
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
        return currentPresenter.getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA ? TabView.State.NOT_AVAILABLE_SHOW_NEXT_AVAILABLE
                : TabView.State.VISIBLE;
    }

    @Override
    public void start(RegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {
        racesListLive = new RacesListLive(currentPresenter, false);
        stage = new EventOverviewStage(currentPresenter);
        statisticsBoxUi = new StatisticsBox(false);
        standingsUi = new StandingsList(currentPresenter.getRegattaMetadata().getState() == RegattaState.FINISHED);

        initWidget(ourUiBinder.createAndBindUi(this));
        
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        refreshManager.add(new RefreshableWidget<RegattaWithProgressDTO>() {
            @Override
            public void setData(RegattaWithProgressDTO data) {
                regattaInfoContainerUi.setWidget(new MultiRegattaListItem(data, false));
            }
        }, new GetRegattaWithProgressAction(myPlace.getCtx().getEventDTO().getId(), myPlace.getRegattaId()));

        stage.setupRefresh(refreshManager);
        refreshManager.add(racesListLive.getRefreshable(), new GetLiveRacesForRegattaAction(currentPresenter.getCtx().getEventDTO()
                .getId(), currentPresenter.getCtx().getRegattaId()));
        refreshManager.add(standingsUi, new GetMiniLeaderbordAction(myPlace.getCtx().getEventDTO().getId(), myPlace.getRegattaId(), 5));
        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(myPlace.getCtx().getEventDTO().getId(), true));
        contentArea.setWidget(this);
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
