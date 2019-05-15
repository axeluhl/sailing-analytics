package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.Util;

public class RaceboardDataDTO implements IsSerializable {
    private RaceWithCompetitorsAndBoatsDTO race;
    private boolean isValidLeaderboardGroup;
    private boolean isValidEvent;
    private ArrayList<DetailType> detailTypesForCompetitorChart;
    private ArrayList<DetailType> availableDetailTypesForLeaderboard;
    private StrippedLeaderboardDTOWithSecurity leaderboard;
    
    // for GWT
    RaceboardDataDTO() {}
    
    public RaceboardDataDTO(RaceWithCompetitorsAndBoatsDTO race,
            boolean isValidLeaderboardGroup, boolean isValidEvent, Iterable<DetailType> detailTypesForCompetitorChart,
            Iterable<DetailType> availableDetailTypesForLeaderboard, StrippedLeaderboardDTOWithSecurity leaderboard) {
        this.race = race;
        this.isValidLeaderboardGroup = isValidLeaderboardGroup;
        this.isValidEvent = isValidEvent;
        this.detailTypesForCompetitorChart = new ArrayList<>();
        Util.addAll(detailTypesForCompetitorChart, this.detailTypesForCompetitorChart);
        this.availableDetailTypesForLeaderboard = new ArrayList<>();
        Util.addAll(availableDetailTypesForLeaderboard, this.availableDetailTypesForLeaderboard);
        this.leaderboard = leaderboard;
    }

    public StrippedLeaderboardDTOWithSecurity getLeaderboard() {
        return leaderboard;
    }

    public Iterable<DetailType> getDetailTypesForCompetitorChart() {
        return detailTypesForCompetitorChart;
    }

    public Iterable<DetailType> getAvailableDetailTypesForLeaderboard() {
        return availableDetailTypesForLeaderboard;
    }

    public Map<CompetitorDTO, BoatDTO> getCompetitorAndTheirBoats() {
        return race.getCompetitorsAndBoats();
    }

    public Iterable<CompetitorDTO> getCompetitors() {
        return race.getCompetitors();
    }

    public RaceWithCompetitorsAndBoatsDTO getRace() {
        return race;
    }

    public boolean isValidLeaderboardGroup() {
        return isValidLeaderboardGroup;
    }

    public boolean isValidEvent() {
        return isValidEvent;
    }
}
