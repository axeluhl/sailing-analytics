package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel.ListNavigationAction;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel.SelectionCallback;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.RaceStateLegend;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListItem;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListStepsLegend;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.AbstractRaceList;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListColumnFactory;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListContainer;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListDataUtil;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn;
import com.sap.sailing.gwt.home.client.place.event.partials.regattaraces.EventRegattaRaces;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetFinishedRacesAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaWithProgressAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sse.common.Util.Triple;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaRacesTabView extends Composite implements RegattaTabView<RegattaRacesPlace> {
    
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    private enum Navigation implements ListNavigationAction {
        SORT_LIST_FORMAT("Sortable list Format TODO", false),
        COMPETITION_FORMAT("Competition Format TODO", true);
        
        private final String displayName;
        private final boolean showAdditionalWidget;
        
        private Navigation(String displayName, boolean showAdditionalWidget) {
            this.displayName = displayName;
            this.showAdditionalWidget = showAdditionalWidget;
        }
        
        @Override
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public boolean isShowAdditionalWidget() {
            return showAdditionalWidget;
        }
    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaRacesTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    @UiField MultiRegattaListStepsLegend regattaProgressLegendUi;
    @UiField SimplePanel regattaInfoContainerUi;
    @UiField(provided = true) ListNavigationPanel<Navigation> listNavigationPanelUi;
    @UiField DivElement listFormatContainerUi;
    @UiField FlowPanel compFormatContainerUi;
    @UiField SimplePanel oldContentContainer;
    @UiField(provided = true) RacesListLive liveRacesListUi;
    @UiField(provided = true) RaceListContainer<RaceListRaceDTO> raceListContainerUi;
    @UiField AnchorElement regattaOverviewUi;

    @Override
    public void setPresenter(Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }
    
    @Override
    public void start(RegattaRacesPlace myPlace, final AcceptsOneWidget contentArea) {
        listNavigationPanelUi = new ListNavigationPanel<Navigation>(new RegattaRacesTabViewNavigationSelectionCallback());
        listNavigationPanelUi.setAdditionalWidget(new RaceStateLegend());
        liveRacesListUi = new RacesListLive(currentPresenter, false);
        raceListContainerUi = new RaceListContainer<>(I18N.finishedRaces(), I18N.noFinishedRaces(), new RaceListFinishedRaces(currentPresenter));
        initWidget(ourUiBinder.createAndBindUi(RegattaRacesTabView.this));
        
        regattaOverviewUi.setHref(currentPresenter.getRegattaOverviewLink());
        if(currentPresenter.isEventOrRegattaLive()) {
            regattaOverviewUi.addClassName(SharedResources.INSTANCE.mainCss().buttonred());
        } else {
            regattaOverviewUi.addClassName(SharedResources.INSTANCE.mainCss().buttonprimary());
        }
        
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        if (ExperimentalFeatures.SHOW_NEW_RACES_LIST) {
            listNavigationPanelUi.removeFromParent(); // TODO temporary removed
    //        listNavigationPanelUi.addAction(Navigation.SORT_LIST_FORMAT, true);
    //        listNavigationPanelUi.addAction(Navigation.COMPETITION_FORMAT, false);
            
            refreshManager.add(liveRacesListUi.getRefreshable(), new GetLiveRacesForRegattaAction(myPlace.getCtx().getEventDTO().getId(), myPlace.getRegattaId()));
            refreshManager.add(new RefreshableWidget<RegattaWithProgressDTO>() {
                @Override
                public void setData(RegattaWithProgressDTO data, long nextUpdate, int updateNo) {
                    regattaInfoContainerUi.setWidget(new MultiRegattaListItem(data));
                }
            }, new GetRegattaWithProgressAction(myPlace.getCtx().getEventDTO().getId(), myPlace.getRegattaId()));
            refreshManager.add(raceListContainerUi, new GetFinishedRacesAction(myPlace.getCtx().getEventDTO().getId(), myPlace.getRegattaId()));
            
            oldContentContainer.removeFromParent();
        } else {
            regattaProgressLegendUi.removeFromParent();
            regattaInfoContainerUi.removeFromParent();
            listFormatContainerUi.removeFromParent();
            compFormatContainerUi.removeFromParent();
            // TODO: understand, and than move this into appropiate place (probably context)
            final String selectedRegattaId = myPlace.getCtx().getRegattaId();
            currentPresenter.ensureRegattaStructure(new AsyncCallback<List<RaceGroupDTO>>() {
                @Override
                public void onSuccess(List<RaceGroupDTO> raceGroups) {
                    if (raceGroups.size() > 0) {
                        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> rs = getRegattaStructure();
                        Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO> selectedRegatta = rs .get(selectedRegattaId);
                        EventRegattaRaces regattaRaces = new EventRegattaRaces(currentPresenter);
                        oldContentContainer.setWidget(regattaRaces);
                        regattaRaces.setRaces(selectedRegatta.getC(), false, selectedRegatta.getB(), selectedRegatta.getA());
                    } else {
                        // createEventWithoutRegattasView(event, panel);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    // createErrorView(
                    // "Error while loading the regatta structure with service getRegattaStructureOfEvent()",
                    // caught, panel);
                }
            });
        }
        contentArea.setWidget(RegattaRacesTabView.this);
    }
    
    @Override
    public void stop() {
    }

    @Override
    public RegattaRacesPlace placeToFire() {
        return new RegattaRacesPlace(currentPresenter.getCtx());
    }

    @Override
    public Class<RegattaRacesPlace> getPlaceClassForActivation() {
        return RegattaRacesPlace.class;
    }
    
    private Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> getRegattaStructure() {
        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> result = new HashMap<>();
        Map<String, RaceGroupDTO> raceGroupsMap = new HashMap<>();
        for (RaceGroupDTO raceGroup : currentPresenter.getCtx().getRaceGroups()) {
            raceGroupsMap.put(raceGroup.getName(), raceGroup);
        }

        for (LeaderboardGroupDTO leaderboardGroup : currentPresenter.getCtx().getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                String leaderboardName = leaderboard.name;
                result.put(leaderboardName, new Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>(
                        raceGroupsMap.get(leaderboardName), leaderboard, leaderboardGroup));
            }
        }
        return result;
    }

    private class RegattaRacesTabViewNavigationSelectionCallback implements SelectionCallback<Navigation> {
        @Override
        public void onSelectAction(Navigation action) {
            showWidget(listFormatContainerUi, action == Navigation.SORT_LIST_FORMAT);
            showWidget(compFormatContainerUi, action == Navigation.COMPETITION_FORMAT);
        }
        
        private void showWidget(Widget widget, boolean show) {
            showWidget(widget.getElement(), show);
        }
        
        private void showWidget(Element widget, boolean show) {
            widget.getStyle().setDisplay(show ? Display.BLOCK : Display.NONE);
        }
    }
    
    private class RaceListFinishedRaces extends AbstractRaceList<RaceListRaceDTO> {
        private final SortableRaceListColumn<RaceListRaceDTO, ?> durationColumn = RaceListColumnFactory.getDurationColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> windSpeedColumn = RaceListColumnFactory.getWindRangeColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> windSourcesCountColumn = RaceListColumnFactory.getWindSourcesCountColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> videoCountColumn = RaceListColumnFactory.getVideoCountColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> audioCountColumn = RaceListColumnFactory.getAudioCountColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> winnerColumn = RaceListColumnFactory.getWinnerColumn();

        public RaceListFinishedRaces(EventView.Presenter presenter) {
            super(presenter);
        }
        
        @Override
        protected void setTableData(Collection<RaceListRaceDTO> data) {
            boolean hasFleets = RaceListDataUtil.hasFleets(data);
            this.fleetCornerColumn.setShowDetails(hasFleets);
            this.fleetNameColumn.setShowDetails(hasFleets);
            this.startTimeColumn.setShowTimeOnly(!RaceListDataUtil.hasDifferentStartDates(data));
            this.durationColumn.setShowDetails(RaceListDataUtil.hasDurations(data));
            boolean hasWind = RaceListDataUtil.hasWind(data);
            this.windSpeedColumn.setShowDetails(hasWind);
            this.windDirectionColumn.setShowDetails(hasWind);
            this.windSourcesCountColumn.setShowDetails(RaceListDataUtil.hasWindSources(data));
            this.videoCountColumn.setShowDetails(RaceListDataUtil.hasVideos(data));
            this.audioCountColumn.setShowDetails(RaceListDataUtil.hasAudios(data));
            super.setTableData(data);
        }

        @Override
        protected void initTableColumns() {
            add(fleetCornerColumn);
            add(raceNameColumn);
            add(fleetNameColumn);
            add(startTimeColumn);
            add(durationColumn);
            add(windSpeedColumn);
            add(windDirectionColumn);
            add(windSourcesCountColumn);
            add(videoCountColumn);
            add(audioCountColumn);
            add(winnerColumn);
            add(raceViewerButtonColumn);
        }
    }

}
