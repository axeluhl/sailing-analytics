package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel.ListNavigationAction;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel.SelectionCallback;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.RaceStateLegend;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.AbstractRaceList;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListContainer;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRaceListViewAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListViewDTO;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaRacesTabView extends Composite implements RegattaTabView<RegattaRacesPlace> {
    
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

    @UiField(provided = true) ListNavigationPanel<Navigation> listNavigationPanelUi;
    @UiField FlowPanel listFormatContainerUi;
    @UiField FlowPanel compFormatContainerUi;

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
        RaceListContainer<LiveRaceDTO> container = new RaceListContainer<LiveRaceDTO>("Finished Races TODO", finishedRacesList = new RaceListFinishedRaces(currentPresenter));
        container.setInfoText("Das ist eine Info!!");
        listFormatContainerUi.add(container);
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }
    
    @Override
    public void start(RegattaRacesPlace myPlace, final AcceptsOneWidget contentArea) {
        listNavigationPanelUi.addAction(Navigation.SORT_LIST_FORMAT, true);
        listNavigationPanelUi.addAction(Navigation.COMPETITION_FORMAT, false);
        
        GetRaceListViewAction action = new GetRaceListViewAction(myPlace.getCtx().getEventDTO().getId());
        currentPresenter.getDispatch().execute(action, new AsyncCallback<ResultWithTTL<RaceListViewDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
            }
            @Override
            public void onSuccess(ResultWithTTL<RaceListViewDTO> result) {
                liveRacesList.setData(result.getDto().getLiveRaces(), 0, 0);
                finishedRacesList.setListData(result.getDto().getAllRaces());
                contentArea.setWidget(RegattaRacesTabView.this);
            }
        });
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
            showWidget(listFormatContainerUi, action == Navigation.SORT_LIST_FORMAT);
            showWidget(compFormatContainerUi, action == Navigation.COMPETITION_FORMAT);
        }
        
        private void showWidget(Widget widget, boolean show) {
            widget.getElement().getStyle().setDisplay(show ? Display.BLOCK : Display.NONE);
        }
    }
    
    private class RaceListFinishedRaces extends AbstractRaceList<LiveRaceDTO> {
        public RaceListFinishedRaces(EventView.Presenter presenter) {
            super(presenter);
        }

        public void setListData(LiveRacesDTO data) {
//            boolean hasFleets = data.hasFleets();
//            this.fleetCornerColumn.setShowDetails(hasFleets);
//            this.fleetNameColumn.setShowDetails(hasFleets);
//            boolean hasWind = data.hasWind();
//            this.windSpeedColumn.setShowDetails(hasWind);
//            this.windDirectionColumn.setShowDetails(hasWind);
            setTableData(data.getRaces());
        }

        @Override
        protected void initTableColumns() {
            add(fleetCornerColumn);
            add(raceNameColumn);
            add(fleetNameColumn);
            add(startTimeColumn);
            add(windSpeedColumn);
            add(windDirectionColumn);
            add(raceViewerButtonColumn);
        }
    }

}