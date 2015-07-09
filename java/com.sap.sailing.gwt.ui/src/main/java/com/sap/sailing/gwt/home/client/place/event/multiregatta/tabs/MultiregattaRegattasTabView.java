package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.DropdownFilter;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.DropdownFilter.DropdownFilterList;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaList;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForEventAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaListViewAction;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaRegattasTabView extends Composite implements MultiregattaTabView<MultiregattaRegattasPlace> {
    
    @UiField(provided = true) DropdownFilter<String> boatCategoryFilterUi;
    @UiField(provided = true) RacesListLive racesListLiveUi;
    @UiField(provided = true) MultiRegattaList regattaListUi;
    private Presenter currentPresenter;

    public MultiregattaRegattasTabView() {

    }

    @Override
    public Class<MultiregattaRegattasPlace> getPlaceClassForActivation() {
        return MultiregattaRegattasPlace.class;
    }
    
    @Override
    public void setPresenter(EventMultiregattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(final MultiregattaRegattasPlace myPlace, final AcceptsOneWidget contentArea) {
        MultiRegattasTabViewRegattaFilterList regattaFilterList = new MultiRegattasTabViewRegattaFilterList();
        boatCategoryFilterUi = new DropdownFilter<String>(StringMessages.INSTANCE.allBoatClasses(), regattaFilterList);
        racesListLiveUi = new RacesListLive(currentPresenter, true);
        regattaListUi = new MultiRegattaList(currentPresenter);
        initWidget(ourUiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        refreshManager.add(racesListLiveUi, new GetLiveRacesForEventAction(currentPresenter.getCtx().getEventDTO().getId()));
        refreshManager.add(regattaFilterList, new GetRegattaListViewAction(currentPresenter.getCtx().getEventDTO().getId()));
        contentArea.setWidget(this);
    }

    @Override
    public void stop() {
    }

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaRegattasTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public MultiregattaRegattasPlace placeToFire() {
        return new MultiregattaRegattasPlace(currentPresenter.getCtx());
    }
    
    private class MultiRegattasTabViewRegattaFilterList implements DropdownFilterList<String>, RefreshableWidget<SortedSetResult<RegattaWithProgressDTO>> {
        
        @Override
        public void setData(SortedSetResult<RegattaWithProgressDTO> data, long nextUpdate, int updateNo) {
            regattaListUi.setData(data, nextUpdate, updateNo);
            boatCategoryFilterUi.updateFilterValues();
        }

        @Override
        public Collection<String> getSelectableValues() {
            return regattaListUi.getSelectableBoatCategories();
        }

        @Override
        public void onSelectFilter(String value) {
            regattaListUi.setVisibleBoatCategory(value);
        }
        
    }

}