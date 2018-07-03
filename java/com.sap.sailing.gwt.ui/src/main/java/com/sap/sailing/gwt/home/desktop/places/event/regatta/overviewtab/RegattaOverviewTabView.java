package com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.communication.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.home.communication.event.GetRegattaStatisticsAction;
import com.sap.sailing.gwt.home.communication.event.GetRegattaWithProgressAction;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.home.communication.event.statistics.GetEventStatisticsAction;
import com.sap.sailing.gwt.home.communication.eventview.HasRegattaMetadata;
import com.sap.sailing.gwt.home.communication.eventview.HasRegattaMetadata.RegattaState;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.desktop.partials.eventdescription.EventDescription;
import com.sap.sailing.gwt.home.desktop.partials.eventstage.EventOverviewStage;
import com.sap.sailing.gwt.home.desktop.partials.liveraces.LiveRacesList;
import com.sap.sailing.gwt.home.desktop.partials.multiregattalist.MultiRegattaListItem;
import com.sap.sailing.gwt.home.desktop.partials.raceoffice.RaceOfficeSection;
import com.sap.sailing.gwt.home.desktop.partials.standings.StandingsList;
import com.sap.sailing.gwt.home.desktop.partials.statistics.DesktopStatisticsBoxView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.shared.partials.statistics.EventStatisticsBox;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManagerWithErrorAndBusy;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

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
    @UiField SimplePanel descriptionUi;
    @UiField(provided = true) StandingsList standingsUi;
    @UiField(provided = true) EventStatisticsBox statisticsBoxUi;
    @UiField RaceOfficeSection raceOfficeSectionUi;
    private final FlagImageResolver flagImageResolver;

    public RegattaOverviewTabView(FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
    }

    @Override
    public Class<RegattaOverviewPlace> getPlaceClassForActivation() {
        return RegattaOverviewPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return State.VISIBLE;
    }

    @Override
    public void start(RegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {
        liveRacesListUi = new LiveRacesList(currentPresenter, false);
        stageUi = new EventOverviewStage(currentPresenter);
        statisticsBoxUi = new EventStatisticsBox(false, new DesktopStatisticsBoxView());
        final HasRegattaMetadata regattaMetadata = currentPresenter.getRegattaMetadata();
        standingsUi = new StandingsList(regattaMetadata != null && regattaMetadata.getState() == RegattaState.FINISHED,
                currentPresenter.getRegattaLeaderboardNavigation(currentPresenter.getRegattaId()),
                flagImageResolver);
        initWidget(ourUiBinder.createAndBindUi(this));

        if (!currentPresenter.showRegattaMetadata()) {
            final String description = currentPresenter.getEventDTO().getDescription();
            if (description != null) {
                descriptionUi.add(new EventDescription(description));
            } else {
                descriptionUi.removeFromParent();
            }
        }
        
        raceOfficeSectionUi.addLink(StringMessages.INSTANCE.racesOverview(), currentPresenter.getRegattaOverviewLink());
        
        RefreshManager refreshManager = new RefreshManagerWithErrorAndBusy(this, contentArea, currentPresenter.getDispatch(), currentPresenter.getErrorAndBusyClientFactory());
        if (currentPresenter.getRegattaMetadata() != null) {
            refreshManager.add(new RefreshableWidget<RegattaWithProgressDTO>() {
                @Override
                public void setData(RegattaWithProgressDTO data) {
                    regattaInfoContainerUi.setWidget(new MultiRegattaListItem(data, true));
                }
            }, new GetRegattaWithProgressAction(currentPresenter.getEventDTO().getId(),
                    currentPresenter.getRegattaId()));
        }

        if (currentPresenter.getEventDTO().isMultiRegatta()) {
            stageUi.removeFromParent();
        } else {
            stageUi.setupRefresh(refreshManager);
        }
        if(currentPresenter.getRegattaMetadata() != null) {
            refreshManager.add(liveRacesListUi.getRefreshable(), new GetLiveRacesForRegattaAction(currentPresenter.getEventDTO()
                    .getId(), currentPresenter.getRegattaId()));
            refreshManager.add(standingsUi, new GetMiniLeaderbordAction(currentPresenter.getEventDTO().getId(), currentPresenter.getRegattaId(), 5));
        }
        refreshManager.add(statisticsBoxUi,
                currentPresenter.getRegattaMetadata() != null
                        ? new GetRegattaStatisticsAction(currentPresenter.getEventDTO().getId(),
                                currentPresenter.getRegattaId())
                        : new GetEventStatisticsAction(currentPresenter.getEventDTO().getId()));
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
