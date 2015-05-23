package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.client.place.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.home.client.place.event.partials.eventregatta.EventRegattaList;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForEventAction;
import com.sap.sse.common.Util.Triple;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaOverviewTabView extends Composite implements MultiregattaTabView<MultiregattaOverviewPlace> {

    private Presenter currentPresenter;

    public MultiregattaOverviewTabView() {

    }

    @Override
    public Class<MultiregattaOverviewPlace> getPlaceClassForActivation() {
        return MultiregattaOverviewPlace.class;
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
    public void start(MultiregattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {
        racesListLive = new RacesListLive(currentPresenter, true);
        stage = new EventOverviewStage(currentPresenter);

        initWidget(ourUiBinder.createAndBindUi(this));
        
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        
        refreshManager.add(stage, new GetEventOverviewStageAction(currentPresenter.getCtx().getEventDTO().getId()));
        refreshManager.add(racesListLive, new GetLiveRacesForEventAction(currentPresenter.getCtx().getEventDTO().getId()));

        contentArea.setWidget(this);

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

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaOverviewTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    
    @UiField SimplePanel content;
    @UiField(provided = true) RacesListLive racesListLive;
    @UiField(provided = true) EventOverviewStage stage;

    @Override
    public MultiregattaOverviewPlace placeToFire() {
        return new MultiregattaOverviewPlace(currentPresenter.getCtx());
    }

}
