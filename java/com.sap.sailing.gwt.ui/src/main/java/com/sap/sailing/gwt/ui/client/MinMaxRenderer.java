package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class MinMaxRenderer {

    private LeaderboardDAO leaderboard;
    private HasStringValue valueProvider;
    private Comparator<LeaderboardRowDAO> comparator;
    
    
    public MinMaxRenderer(LeaderboardDAO leaderboard, HasStringValue valueProvider,
            Comparator<LeaderboardRowDAO> comparator) {
        this.leaderboard = leaderboard;
        this.valueProvider = valueProvider;
        this.comparator = comparator;
    }

    public void render(Context context, LeaderboardRowDAO row, SafeHtmlBuilder sb) {
        
    }

    public void updateMinMax(LeaderboardDAO leaderboard) {
        
    }

}
