package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public class LeaderboardGroupImpl implements LeaderboardGroup {
    
    private String name;
    private String description;
    private ArrayList<Leaderboard> leaderboards;

    public LeaderboardGroupImpl(String name, String description, List<Leaderboard> leaderboards) {
        this.name = name;
        this.description = description;
        this.leaderboards = new ArrayList<Leaderboard>(leaderboards);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Leaderboard> getLeaderboards() {
        return leaderboards;
    }

    @Override
    public void addLeaderboard(Leaderboard leaderboard) {
        leaderboards.add(leaderboard);
    }

    @Override
    public void removeLeaderboard(Leaderboard leaderboard) {
        leaderboards.remove(leaderboard);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescriptiom(String description) {
        this.description = description;
    }

}
