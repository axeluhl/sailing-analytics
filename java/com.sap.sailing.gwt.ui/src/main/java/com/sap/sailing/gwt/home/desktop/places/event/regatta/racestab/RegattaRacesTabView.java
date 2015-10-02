package com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
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
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.TextBoxFilter;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.TextBoxFilter.TextBoxFilterChangeHandler;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.refresh.ActionProvider.AbstractActionProvider;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
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
import com.sap.sailing.gwt.ui.shared.dispatch.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.AbstractTextFilter;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaRacesTabView extends Composite implements RegattaTabView<RegattaRacesPlace> {
    
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    private enum Navigation implements ListNavigationAction {
        SORT_LIST_FORMAT(I18N.listFormatLabel(), true),
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
    
    private RaceListFinishedRaces finishedRacesList;
    @Override
    public void start(RegattaRacesPlace myPlace, final AcceptsOneWidget contentArea) {
        CompetitorFilterHandler competitorFilterHandler = new CompetitorFilterHandler();
        TextBoxFilter competitorFilterUi = new TextBoxFilter();
        listNavigationPanelUi = new ListNavigationPanel<Navigation>(new RegattaRacesTabViewNavigationSelectionCallback());
        listNavigationPanelUi.setAdditionalWidget(competitorFilterUi);
        liveRacesListUi = new LiveRacesList(currentPresenter, false);
        finishedRacesList = new RaceListFinishedRaces(currentPresenter);
        finishedRacesList.initTableFilter(competitorFilterHandler.raceFilter);
        raceListContainerUi = new RaceListContainer<>(I18N.finishedRaces(), I18N.noFinishedRaces(), finishedRacesList);
        initWidget(ourUiBinder.createAndBindUi(RegattaRacesTabView.this));
        competitorFilterUi.addValueChangeHandler(competitorFilterHandler);
        raceOfficeSectionUi.addLink(I18N.racesOverview(), currentPresenter.getRegattaOverviewLink());
        
        refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
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
            addRacesAction(competitorFilterHandler, new GetCompetitionFormatRacesAction(eventId, regattaId), Navigation.COMPETITION_FORMAT);
        } else {
            listNavigationPanelUi.removeFromParent();
            compFormatContainerUi.removeFromParent();
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
    
    private class RegattaRacesTabViewNavigationSelectionCallback implements SelectionCallback<Navigation> {

        @Override
        public void onSelectAction(Navigation action) {
            RegattaRacesTabView.this.currentlySelectedTab = action;
            RegattaRacesTabView.this.refreshManager.forceReschule();
            UIObject.setVisible(listFormatContainerUi, action == Navigation.SORT_LIST_FORMAT);
            compFormatContainerUi.setVisible(action == Navigation.COMPETITION_FORMAT);
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
    
    private class RaceFilter extends AbstractTextFilter<SimpleRaceMetadataDTO> {
        
        private List<String> keywords = new ArrayList<>();
            
        private AbstractListFilter<SimpleCompetitorDTO> listFilter = new AbstractListFilter<SimpleCompetitorDTO>() {
            @Override
            public Iterable<String> getStrings(SimpleCompetitorDTO t) {
                return Arrays.asList(t.getName(), t.getSailID());
            }
        };
        
        @Override
        public boolean matches(SimpleRaceMetadataDTO object) {
            return keywords.isEmpty() || !Util.isEmpty(listFilter.applyFilter(keywords, object.getCompetitors()));
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }
        
    }
    
    private class CompetitorFilterHandler implements TextBoxFilterChangeHandler, RefreshableWidget<ListResult<RaceCompetitionFormatSeriesDTO>> {

        private RaceFilter raceFilter = new RaceFilter();

        @Override
        public void setData(ListResult<RaceCompetitionFormatSeriesDTO> data) {
            compFormatContainerUi.clear();
            for (RaceCompetitionFormatSeriesDTO series : data.getValues()) {
                compFormatContainerUi.add(new RegattaCompetitionSeries(currentPresenter, series));
            }
            this.update();
        }

        @Override
        public void onFilterChanged(String searchString) {
            this.raceFilter.keywords.clear();
            if (searchString != null && !searchString.isEmpty()) {
                this.raceFilter.keywords.add(searchString);
            }
            finishedRacesList.update();
            this.update();
        }
        
        private void update() {
            for (int i = 0; i < compFormatContainerUi.getWidgetCount(); i++) {
                RegattaCompetitionSeries series = (RegattaCompetitionSeries) compFormatContainerUi.getWidget(i);
                series.setRacesFilter(raceFilter);
            }
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
        
        private void update() {
            cellTable.redraw();
        }
    }

}
