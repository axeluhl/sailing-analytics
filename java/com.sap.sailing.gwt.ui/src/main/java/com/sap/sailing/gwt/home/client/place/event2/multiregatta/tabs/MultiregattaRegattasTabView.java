package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.client.place.event2.partials.eventregatta.EventRegattaList;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util.Triple;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaRegattasTabView extends Composite implements MultiregattaTabView<MultiregattaRegattasPlace> {
    
    @UiField FlowPanel content;
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
        
     // TODO: understand, and than move this into appropiate place (probably context)
        currentPresenter.ensureRegattaStructure(new AsyncCallback<List<RaceGroupDTO>>() {
                    @Override
                    public void onSuccess(List<RaceGroupDTO> raceGroups) {
                        if (raceGroups.size() > 0) {
                            initView(raceGroups, contentArea);
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

    protected void initView(List<RaceGroupDTO> raceGroups, AcceptsOneWidget contentArea) {
        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> regattaStructure = getRegattaStructure(currentPresenter.getCtx().getEventDTO(), raceGroups);
        EventRegattaList eventRegattaList = new EventRegattaList(regattaStructure, currentPresenter);
//        initWidget(ourUiBinder.createAndBindUi(this));
//        contentArea.setWidget(this);
        contentArea.setWidget(eventRegattaList);
    }
    
    private Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> getRegattaStructure(EventDTO event, List<RaceGroupDTO> raceGroups) {
        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> result = new HashMap<>();
        Map<String, RaceGroupDTO> raceGroupsMap = new HashMap<>();
        for (RaceGroupDTO raceGroup: raceGroups) {
            raceGroupsMap.put(raceGroup.getName(), raceGroup);
        }            
        
        for (LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
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

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaRegattasTabView> {
    }

    @SuppressWarnings("unused")
    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public MultiregattaRegattasPlace placeToFire() {
        return new MultiregattaRegattasPlace(currentPresenter.getCtx());
    }

}