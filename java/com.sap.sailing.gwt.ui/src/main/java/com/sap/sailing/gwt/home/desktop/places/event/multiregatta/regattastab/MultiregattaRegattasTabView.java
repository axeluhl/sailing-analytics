package com.sap.sailing.gwt.home.desktop.places.event.multiregatta.regattastab;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.communication.event.GetLiveRacesForEventAction;
import com.sap.sailing.gwt.home.communication.event.GetRegattaListViewAction;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.desktop.partials.liveraces.LiveRacesList;
import com.sap.sailing.gwt.home.desktop.partials.multiregattalist.MultiRegattaList;
import com.sap.sailing.gwt.home.desktop.partials.raceoffice.RaceOfficeSection;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.DropdownFilter;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.DropdownFilter.DropdownFilterList;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.shared.refresh.RefreshManagerWithErrorAndBusy;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaRegattasTabView extends Composite implements MultiregattaTabView<MultiregattaRegattasPlace> {
    
    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaRegattasTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    
    @UiField(provided = true) LiveRacesList liveRacesListUi;
    @UiField(provided = true) DropdownFilter<String> boatCategoryFilterUi;
    @UiField(provided = true) MultiRegattaList regattaListUi;
    @UiField RaceOfficeSection raceOfficeSectionUi;
    private Presenter currentPresenter;

    @Override
    public Class<MultiregattaRegattasPlace> getPlaceClassForActivation() {
        return MultiregattaRegattasPlace.class;
    }
    
    @Override
    public void setPresenter(EventMultiregattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public void start(final MultiregattaRegattasPlace myPlace, final AcceptsOneWidget contentArea) {
        liveRacesListUi = new LiveRacesList(currentPresenter, true);
        MultiregattaRegattasTabViewRegattaFilterList regattaFilterList = new MultiregattaRegattasTabViewRegattaFilterList();
        boatCategoryFilterUi = new DropdownFilter<String>(StringMessages.INSTANCE.all(), regattaFilterList);
        regattaListUi = new MultiRegattaList(currentPresenter, true);
        initWidget(ourUiBinder.createAndBindUi(this));
        raceOfficeSectionUi.addLink(StringMessages.INSTANCE.racesOverview(), currentPresenter.getRegattaOverviewLink());
        
        RefreshManager refreshManager = new RefreshManagerWithErrorAndBusy(this, contentArea, currentPresenter.getDispatch(), currentPresenter.getErrorAndBusyClientFactory());
        refreshManager.add(liveRacesListUi.getRefreshable(), new GetLiveRacesForEventAction(currentPresenter.getEventDTO().getId()));
        
        refreshManager.add(regattaFilterList, new GetRegattaListViewAction(currentPresenter.getEventDTO().getId()));
    }
    
    @Override
    public void stop() {
    }

    @Override
    public MultiregattaRegattasPlace placeToFire() {
        return new MultiregattaRegattasPlace(currentPresenter.getCtx());
    }
    
    private class MultiregattaRegattasTabViewRegattaFilterList implements 
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
