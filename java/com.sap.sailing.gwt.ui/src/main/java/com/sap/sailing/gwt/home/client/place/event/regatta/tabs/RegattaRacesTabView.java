package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.regattaraces.EventRegattaRaces;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.ActionProvider.AbstractActionProvider;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.desktop.partials.liveraces.LiveRacesList;
import com.sap.sailing.gwt.home.desktop.partials.multiregattalist.MultiRegattaListItem;
import com.sap.sailing.gwt.home.desktop.partials.racelist.AbstractRaceList;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListColumnFactory;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListColumnSet;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListContainer;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListDataUtil;
import com.sap.sailing.gwt.home.desktop.partials.racelist.SortableRaceListColumn;
import com.sap.sailing.gwt.home.desktop.partials.raceoffice.RaceOfficeSection;
import com.sap.sailing.gwt.home.desktop.partials.regattacompetition.RegattaCompetitionSeries;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.ListNavigationPanel;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.ListNavigationPanel.ListNavigationAction;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.ListNavigationPanel.SelectionCallback;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.partials.regattanavigation.RaceStateLegend;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetCompetitionFormatRacesAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetFinishedRacesAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaWithProgressAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sse.common.Util.Triple;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaRacesTabView extends Composite implements RegattaTabView<RegattaRacesPlace> {
    
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    private enum Navigation implements ListNavigationAction {
        SORT_LIST_FORMAT(I18N.listFormatLabel(), false),
        COMPETITION_FORMAT(I18N.competitionFormatLabel(), true);
        
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

    interface MyBinder extends UiBinder<Widget, RegattaRacesTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    @UiField SimplePanel regattaInfoContainerUi;
    @UiField(provided = true) ListNavigationPanel<Navigation> listNavigationPanelUi;
    @UiField DivElement listFormatContainerUi;
    @UiField FlowPanel compFormatContainerUi;
    @UiField SimplePanel oldContentContainer;
    @UiField(provided = true) LiveRacesList liveRacesListUi;
    @UiField(provided = true) RaceListContainer<RaceListRaceDTO> raceListContainerUi;
    @UiField RaceOfficeSection raceOfficeSectionUi;
    private Navigation currentlySelectedTab = Navigation.SORT_LIST_FORMAT;
    private RefreshManager refreshManager;
    
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
        liveRacesListUi = new LiveRacesList(currentPresenter, false);
        raceListContainerUi = new RaceListContainer<>(I18N.finishedRaces(), I18N.noFinishedRaces(), new RaceListFinishedRaces(currentPresenter));
        initWidget(ourUiBinder.createAndBindUi(RegattaRacesTabView.this));
        raceOfficeSectionUi.addLink(I18N.racesOverview(), currentPresenter.getRegattaOverviewLink());
        
        refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        if (ExperimentalFeatures.SHOW_NEW_RACES_LIST) {
            UUID eventId = myPlace.getCtx().getEventDTO().getId();
            String regattaId = myPlace.getRegattaId();
            refreshManager.add(new RefreshableWidget<RegattaWithProgressDTO>() {
                @Override
                public void setData(RegattaWithProgressDTO data) {
                    regattaInfoContainerUi.setWidget(new MultiRegattaListItem(data, true));
                }
            }, new GetRegattaWithProgressAction(eventId, regattaId));
            addRacesAction(liveRacesListUi.getRefreshable(), new GetLiveRacesForRegattaAction(eventId, regattaId), Navigation.SORT_LIST_FORMAT);
            addRacesAction(raceListContainerUi, new GetFinishedRacesAction(eventId, regattaId), Navigation.SORT_LIST_FORMAT);
            
            if (ExperimentalFeatures.SHOW_RACES_COMPETITION_FORMAT) {
                listNavigationPanelUi.addAction(Navigation.SORT_LIST_FORMAT, true);
                listNavigationPanelUi.addAction(Navigation.COMPETITION_FORMAT, false);
                addRacesAction(new RefreshableWidget<ListResult<RaceCompetitionFormatSeriesDTO>>() {
                    @Override
                    public void setData(ListResult<RaceCompetitionFormatSeriesDTO> data) {
                        compFormatContainerUi.clear();
                        for (RaceCompetitionFormatSeriesDTO series : data.getValues()) {
                            compFormatContainerUi.add(new RegattaCompetitionSeries(currentPresenter, series));
                        }
                    }
                }, new GetCompetitionFormatRacesAction(eventId, regattaId), Navigation.COMPETITION_FORMAT);
            } else {
                listNavigationPanelUi.removeFromParent();
                compFormatContainerUi.removeFromParent();
            }
            oldContentContainer.removeFromParent();
        } else {
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
    
    private <D extends DTO, A extends Action<ResultWithTTL<D>>> void addRacesAction(
            RefreshableWidget<? super D> widget, A action, Navigation assosiatedTab) {
        refreshManager.add(widget, new RegattaRacesTabViewActionProvider<>(action, assosiatedTab));
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
            RegattaRacesTabView.this.currentlySelectedTab = action;
            RegattaRacesTabView.this.refreshManager.forceReschule();
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
    
    private class RegattaRacesTabViewActionProvider<A extends Action<?>> extends AbstractActionProvider<A> {
        private final Navigation assosiatedTab;

        public RegattaRacesTabViewActionProvider(A action, Navigation assosiatedTab) {
            super(action);
            this.assosiatedTab = assosiatedTab;
        }
        
        @Override
        public boolean isActive() {
            return assosiatedTab == currentlySelectedTab;
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
            super(presenter, new RaceListColumnSet(1, 1));
        }
        
        @Override
        protected void setTableData(Collection<RaceListRaceDTO> data) {
            boolean hasFleets = RaceListDataUtil.hasFleets(data);
            this.fleetCornerColumn.setShowDetails(hasFleets);
            this.fleetNameColumn.setShowDetails(hasFleets);
            // Imagine a long running event (several days) where only one race has taken place.
            // Actually, you can't find out the date of this race, only its start time.
            // Therefore, start date and time is shown, as long as there's now grouping feature. 
            // TODO: this.startTimeColumn.setShowTimeOnly(!RaceListDataUtil.hasDifferentStartDates(data));
            this.startTimeColumn.setShowTimeOnly(false);
            this.durationColumn.setShowDetails(RaceListDataUtil.hasDurations(data));
            boolean hasWind = RaceListDataUtil.hasWind(data);
            this.windSpeedColumn.setShowDetails(hasWind);
            this.windDirectionColumn.setShowDetails(hasWind);
            this.windSourcesCountColumn.setShowDetails(RaceListDataUtil.hasWindSources(data));
            this.videoCountColumn.setShowDetails(RaceListDataUtil.hasVideos(data));
            this.audioCountColumn.setShowDetails(RaceListDataUtil.hasAudios(data));
            this.winnerColumn.setShowDetails(RaceListDataUtil.hasWinner(data));
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
            
            columnSet.addColumn(windSpeedColumn);
            columnSet.addColumn(windDirectionColumn);
            columnSet.addColumn(durationColumn);
            columnSet.addColumn(windSourcesCountColumn);
            columnSet.addColumn(videoCountColumn);
            columnSet.addColumn(audioCountColumn);
            columnSet.addColumn(fleetNameColumn);
        }
    }

}
