package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.gwt.settings.client.raceboard.FullAnalysisMode;

public enum RaceBoardModes {
    PLAYER(new PlayerMode()), FULL_ANALYSIS(new FullAnalysisMode()), START_ANALYSIS(new StartAnalysisMode()), WINNING_LANES(new WinningLanesMode());
    
    private RaceBoardModes(RaceBoardMode mode) {
        this.mode = mode;
    }
    
    public RaceBoardMode getMode() {
        return mode;
    }

    private final RaceBoardMode mode;
}
