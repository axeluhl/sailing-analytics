package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public class LeaderboardGroupImpl implements LeaderboardGroup {
    
    private static final long serialVersionUID = 2035927369446736934L;
    private String name;
    private String description;
    private List<Leaderboard> leaderboards;

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
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescriptiom(String description) {
        this.description = description;
    }

    @Override
    public synchronized Iterable<Leaderboard> getLeaderboards() {
        return new ArrayList<Leaderboard>(leaderboards);
    }

    @Override
    public int getIndexOf(Leaderboard leaderboard) {
        return leaderboards.indexOf(leaderboard);
    }

    @Override
    public synchronized void addLeaderboard(Leaderboard leaderboard) {
        leaderboards.add(leaderboard);
    }
    
    @Override
    public synchronized void addLeaderboardAt(Leaderboard leaderboard, int index) {
        leaderboards.add(index, leaderboard);
    }

    @Override
    public synchronized void addAllLeaderboards(Collection<Leaderboard> leaderboards) {
        this.leaderboards.addAll(leaderboards);
    }

    @Override
    public synchronized void removeLeaderboard(Leaderboard leaderboard) {
        leaderboards.remove(leaderboard);
    }

    @Override
    public synchronized void removeAllLeaderboards(Collection<Leaderboard> leaderboards) {
        this.leaderboards.removeAll(leaderboards);
    }

    @Override
    public synchronized void clearLeaderboards() {
        leaderboards.clear();
    }

}
