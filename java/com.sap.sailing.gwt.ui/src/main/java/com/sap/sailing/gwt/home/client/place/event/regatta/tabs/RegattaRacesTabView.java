package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
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
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel.ListNavigationAction;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel.SelectionCallback;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.RaceStateLegend;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListResources;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListSteps;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.AbstractRaceList;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListColumnFactory;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListContainer;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListDataUtil;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn;
import com.sap.sailing.gwt.home.client.place.event.partials.regattaraces.EventRegattaRaces;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRaceListViewAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListViewDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressDTO;
import com.sap.sse.common.Util.Triple;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaRacesTabView extends Composite implements RegattaTabView<RegattaRacesPlace> {
    
    private static final boolean SHOW_NEW_RACES_LIST = true;
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

    @UiField SimplePanel regattaInfoContainerUi;
    @UiField(provided = true) ListNavigationPanel<Navigation> listNavigationPanelUi;
    @UiField FlowPanel listFormatContainerUi;
    @UiField FlowPanel compFormatContainerUi;
    @UiField SimplePanel oldContentContainer;

    private RacesListLive liveRacesList;
    private RaceListFinishedRaces finishedRacesList;

    public RegattaRacesTabView() {
        listNavigationPanelUi = new ListNavigationPanel<Navigation>(new RegattaRacesTabViewNavigationSelectionCallback());
        listNavigationPanelUi.setAdditionalWidget(new RaceStateLegend());
        initWidget(ourUiBinder.createAndBindUi(RegattaRacesTabView.this));
    }
    
    @Override
    public void setPresenter(Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        listFormatContainerUi.add(liveRacesList = new RacesListLive(currentPresenter, false));
        listFormatContainerUi.add(new RaceListContainer<>(I18N.finishedRaces(), finishedRacesList = new RaceListFinishedRaces(currentPresenter)));
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }
    
    @Override
    public void start(RegattaRacesPlace myPlace, final AcceptsOneWidget contentArea) {
        if (SHOW_NEW_RACES_LIST) {
            listNavigationPanelUi.removeFromParent(); // TODO temporary removed
    //        listNavigationPanelUi.addAction(Navigation.SORT_LIST_FORMAT, true);
    //        listNavigationPanelUi.addAction(Navigation.COMPETITION_FORMAT, false);
            
            GetRaceListViewAction action = new GetRaceListViewAction(myPlace.getCtx().getEventDTO().getId(), myPlace.getRegattaId());
            currentPresenter.getDispatch().execute(action, new AsyncCallback<ResultWithTTL<RaceListViewDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                }
                @Override
                public void onSuccess(ResultWithTTL<RaceListViewDTO> result) {
                    regattaInfoContainerUi.setWidget(getRegattaInformation(result.getDto().getProgress()));
                    liveRacesList.setListData(result.getDto().getLiveRaces());
                    finishedRacesList.setListData(result.getDto().getFinishedRaces());
    //                for (RaceListSeriesDTO series : result.getDto().getRacesForCompetitionFormat()) {
    //                    compFormatContainerUi.add(new RegattaCompetitionSeries(currentPresenter, series));
    //                }
                    contentArea.setWidget(RegattaRacesTabView.this);
                }
            });
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
                        contentArea.setWidget(RegattaRacesTabView.this);
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
    }
    
    private Widget getRegattaInformation(RegattaProgressDTO regattaProgress) {
        MultiRegattaListSteps regattaProgressUi = new MultiRegattaListSteps(regattaProgress);
        regattaProgressUi.addStyleName(MultiRegattaListResources.INSTANCE.css().regattalistitem());
        return regattaProgressUi;
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
            widget.getElement().getStyle().setDisplay(show ? Display.BLOCK : Display.NONE);
        }
    }
    
    private class RaceListFinishedRaces extends AbstractRaceList<RaceListRaceDTO> {
        private final SortableRaceListColumn<RaceListRaceDTO, ?> durationColumn = RaceListColumnFactory.getDurationColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> windfixesCountColumn = RaceListColumnFactory.getWindFixesCountColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> videoCountColumn = RaceListColumnFactory.getVideoCountColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> audioCountColumn = RaceListColumnFactory.getAudioCountColumn();
        private final SortableRaceListColumn<RaceListRaceDTO, ?> winnerColumn = RaceListColumnFactory.getWinnerColumn();
        
        public RaceListFinishedRaces(EventView.Presenter presenter) {
            super(presenter);
        }

        public void setListData(Collection<RaceListRaceDTO> data) {
            boolean hasFleets = RaceListDataUtil.hasFleets(data);
            this.fleetCornerColumn.setShowDetails(hasFleets);
            this.fleetNameColumn.setShowDetails(hasFleets);
            this.durationColumn.setShowDetails(RaceListDataUtil.hasDurations(data));
            boolean hasWind = RaceListDataUtil.hasWind(data);
            this.windSpeedColumn.setShowDetails(hasWind);
            this.windDirectionColumn.setShowDetails(hasWind);
            this.windfixesCountColumn.setShowDetails(RaceListDataUtil.hasWindFixes(data));
            this.videoCountColumn.setShowDetails(RaceListDataUtil.hasVideos(data));
            this.audioCountColumn.setShowDetails(RaceListDataUtil.hasAudios(data));
            setTableData(data);
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
            add(windfixesCountColumn);
            add(videoCountColumn);
            add(audioCountColumn);
            add(winnerColumn);
            add(raceViewerButtonColumn);
        }
    }

}