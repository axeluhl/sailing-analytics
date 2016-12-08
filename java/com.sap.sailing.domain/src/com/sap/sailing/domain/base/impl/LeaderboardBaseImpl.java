package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.LeaderboardBase;
import com.sap.sailing.domain.base.LeaderboardChangeListener;

public class LeaderboardBaseImpl implements LeaderboardBase {
    private static final long serialVersionUID = -5332972141344617372L;
    private final String name;
    private final String displayName;
    
    public LeaderboardBaseImpl(String name, String displayName) {
        super();
        this.name = name;
        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void addLeaderboardChangeListener(LeaderboardChangeListener listener) {
    }

    @Override
    public void removeLeaderboardChangeListener(LeaderboardChangeListener listener) {
    }
}
