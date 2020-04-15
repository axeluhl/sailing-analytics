package com.sap.sailing.gwt.home.desktop.places.event.multiregatta.overviewtab;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.home.communication.event.GetLiveRacesForEventAction;
import com.sap.sailing.gwt.home.communication.event.GetRegattaListViewAction;
import com.sap.sailing.gwt.home.communication.event.statistics.GetEventStatisticsAction;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.desktop.partials.eventdescription.EventDescription;
import com.sap.sailing.gwt.home.desktop.partials.eventstage.EventOverviewStage;
import com.sap.sailing.gwt.home.desktop.partials.liveraces.LiveRacesList;
import com.sap.sailing.gwt.home.desktop.partials.multiregattalist.MultiRegattaList;
import com.sap.sailing.gwt.home.desktop.partials.raceoffice.RaceOfficeSection;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.DropdownFilter;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.DropdownFilter.DropdownFilterList;
import com.sap.sailing.gwt.home.desktop.partials.statistics.DesktopStatisticsBoxView;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.shared.partials.statistics.EventStatisticsBox;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManagerWithErrorAndBusy;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaOverviewTabView extends Composite implements MultiregattaTabView<MultiregattaOverviewPlace> {

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaOverviewTabView> {
    }
    
    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    
    @UiField(provided = true) EventOverviewStage stageUi;
    @UiField SimplePanel descriptionUi;
    @UiField(provided = true) LiveRacesList liveRacesListUi;
    @UiField(provided = true) DropdownFilter<String> boatCategoryFilterUi;
    @UiField(provided = true) MultiRegattaList regattaListUi;
    @UiField(provided = true) EventStatisticsBox statisticsBoxUi;
    @UiField RaceOfficeSection raceOfficeSectionUi;
    private Presenter currentPresenter;

    @Override
    public Class<MultiregattaOverviewPlace> getPlaceClassForActivation() {
        return MultiregattaOverviewPlace.class;
    }
    
    @Override
    public void setPresenter(EventMultiregattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public void start(MultiregattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {
        stageUi = new EventOverviewStage(currentPresenter);
        liveRacesListUi = new LiveRacesList(currentPresenter, true);
        MultiregattaOverviewRegattasTabViewRegattaFilterList regattaFilterList = new MultiregattaOverviewRegattasTabViewRegattaFilterList();
        boatCategoryFilterUi = new DropdownFilter<String>(StringMessages.INSTANCE.all(), regattaFilterList);
        regattaListUi = new MultiRegattaList(currentPresenter, false);
        statisticsBoxUi = new EventStatisticsBox(true, new DesktopStatisticsBoxView());
        
        initWidget(ourUiBinder.createAndBindUi(this));

        final String description = currentPresenter.getEventDTO().getDescription();
        if (description != null) {
            descriptionUi.add(new EventDescription(description));
        } else {
            descriptionUi.removeFromParent();
        }
        
        raceOfficeSectionUi.addLink(StringMessages.INSTANCE.racesOverview(), currentPresenter.getRegattaOverviewLink());
        
        RefreshManager refreshManager = new RefreshManagerWithErrorAndBusy(this, contentArea, currentPresenter.getDispatch(), currentPresenter.getErrorAndBusyClientFactory());
        stageUi.setupRefresh(refreshManager);
        refreshManager.add(liveRacesListUi.getRefreshable(), new GetLiveRacesForEventAction(currentPresenter.getEventDTO().getId()));
        
        refreshManager.add(regattaFilterList, new GetRegattaListViewAction(currentPresenter.getEventDTO().getId()));
        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(currentPresenter.getEventDTO().getId()));
    }

    @Override
    public void stop() {
    }
    
    @Override
    public MultiregattaOverviewPlace placeToFire() {
        return new MultiregattaOverviewPlace(currentPresenter.getCtx());
    }
    
    private class MultiregattaOverviewRegattasTabViewRegattaFilterList implements 
            DropdownFilterList<String>, RefreshableWidget<SortedSetResult<RegattaWithProgressDTO>> {
        @Override
        public void setData(SortedSetResult<RegattaWithProgressDTO> data) {
            regattaListUi.setData(data);
            boatCategoryFilterUi.updateFilterValues();
        }
        @Override
        public Collection<String> getSelectableValues() {
            return regattaListUi.getSelectableBoatCategories();
        }
        @Override
        public void onSelectFilter(String value) {
            regattaListUi.setVisibleLeaderboardGroup(value);
        }
    }

}
