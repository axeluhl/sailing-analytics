package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MainCss;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.client.place.event.partials.eventregatta.EventRegattaList;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaList;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListStepsLegend;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.DropdownFilter;
import com.sap.sailing.gwt.home.desktop.partials.regattanavigation.DropdownFilter.DropdownFilterList;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForEventAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaListViewAction;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sse.common.Util.Triple;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaRegattasTabView extends Composite implements MultiregattaTabView<MultiregattaRegattasPlace> {
    
    private static final MainCss MAIN_CSS = SharedResources.INSTANCE.mainCss();

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaRegattasTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    
    @UiField SimplePanel content;
    @UiField DivElement newContentContainerUi;
    @UiField(provided = true) RacesListLive racesListLiveUi;
    @UiField DivElement legendAndFilterUi;
    @UiField(provided = true) DropdownFilter<String> boatCategoryFilterUi;
    @UiField MultiRegattaListStepsLegend regattaProgressLegendUi;
    @UiField(provided = true) MultiRegattaList regattaListUi;
    @UiField AnchorElement regattaOverviewLinkUi;
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
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(final MultiregattaRegattasPlace myPlace, final AcceptsOneWidget contentArea) {
        racesListLiveUi = new RacesListLive(currentPresenter, true);
        MultiregattaRegattasTabViewRegattaFilterList regattaFilterList = new MultiregattaRegattasTabViewRegattaFilterList();
        boatCategoryFilterUi = new DropdownFilter<String>(StringMessages.INSTANCE.allBoatClasses(), regattaFilterList);
        regattaListUi = new MultiRegattaList(currentPresenter, true);
        
        initWidget(ourUiBinder.createAndBindUi(this));
        regattaOverviewLinkUi.setHref(currentPresenter.getRegattaOverviewLink());
        regattaOverviewLinkUi.addClassName(currentPresenter.isEventOrRegattaLive() ? MAIN_CSS.buttonred() : MAIN_CSS.buttonprimary());
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        refreshManager.add(racesListLiveUi.getRefreshable(), new GetLiveRacesForEventAction(currentPresenter.getCtx().getEventDTO().getId()));
        
        if (ExperimentalFeatures.SHOW_NEW_REGATTA_LIST) {
            refreshManager.add(regattaFilterList, new GetRegattaListViewAction(currentPresenter.getCtx().getEventDTO().getId()));
            content.removeFromParent();
        } else {
            boatCategoryFilterUi.removeFromParent();
            regattaProgressLegendUi.removeFromParent();
            regattaListUi.removeFromParent();
            newContentContainerUi.removeFromParent();
            // TODO: understand, and than move this into appropiate place (probably context)
            currentPresenter.ensureRegattaStructure(new AsyncCallback<List<RaceGroupDTO>>() {
                @Override
                public void onSuccess(List<RaceGroupDTO> raceGroups) {
                    if (raceGroups.size() > 0) {
                        initView();
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
        contentArea.setWidget(this);
    }
    
    protected void initView() {
        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> regattaStructure = getRegattaStructure();
        EventRegattaList eventRegattaList = new EventRegattaList(regattaStructure, currentPresenter);
        content.setWidget(eventRegattaList);
    }
    
    private Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> getRegattaStructure() {
        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> result = new HashMap<>();
        Map<String, RaceGroupDTO> raceGroupsMap = new HashMap<>();
        for (RaceGroupDTO raceGroup: currentPresenter.getCtx().getRaceGroups()) {
            raceGroupsMap.put(raceGroup.getName(), raceGroup);
        }            
        
        for (LeaderboardGroupDTO leaderboardGroup : currentPresenter.getCtx().getLeaderboardGroups()) {
            for(StrippedLeaderboardDTO leaderboard: leaderboardGroup.getLeaderboards()) {
                String leaderboardName = leaderboard.name;
                result.put(leaderboardName, new Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>(raceGroupsMap.get(leaderboardName),
                        leaderboard, leaderboardGroup));
            }
        }
        return result;
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
            legendAndFilterUi.getStyle().setDisplay(data.isEmpty() ? Display.NONE : Display.BLOCK);
            regattaListUi.setData(data);
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
