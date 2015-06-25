package com.sap.sailing.gwt.home.client.place.event.regatta.tabs;

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
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.AbstractRaceList;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListContainer;
import com.sap.sailing.gwt.home.client.place.event.partials.regattaraces.EventRegattaRaces;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaRacesTabView extends Composite implements RegattaTabView<RegattaRacesPlace> {

    interface MyBinder extends UiBinder<HTMLPanel, RegattaRacesTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    @UiField FlowPanel latestUpdateContainer;
    @UiField SimplePanel oldContentContainer;

    public RegattaRacesTabView() {
        initWidget(ourUiBinder.createAndBindUi(RegattaRacesTabView.this));
    }

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
        final String selectedRegattaId = myPlace.getCtx().getRegattaId();
        
        currentPresenter.getDispatch().execute(new GetLiveRacesForRegattaAction(myPlace.getCtx().getEventDTO().getId(),
                selectedRegattaId), new AsyncCallback<ResultWithTTL<LiveRacesDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(ResultWithTTL<LiveRacesDTO> result) {
                String date = DateAndTimeFormatterUtil.longDateFormatter.render(MillisecondsTimePoint.now().asDate());
                RaceListFinishedRaces raceList = new RaceListFinishedRaces(currentPresenter);
                latestUpdateContainer.add(new RaceListContainer<LiveRaceDTO>(date, raceList));;
                raceList.setListData(result.getDto());
            }
        });
        
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
                    
                }
            }

            @Override
            public void onFailure(Throwable caught) {
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