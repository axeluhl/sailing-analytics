package com.sap.sailing.domain.common;

public enum LeaderboardType {
    RegattaLeaderboard(false, true), RegattaMetaLeaderboard(true, true),
    RegattaLeaderboardWithEliminations(false, true),
    FlexibleLeaderboard(false, false), FlexibleMetaLeaderboard(true, false);
    
    boolean isRegattaLeaderboard;
    boolean isMetaLeaderboard;
    
    LeaderboardType(boolean isMetaLeaderboard, boolean isRegattaLeaderboard) {
        this.isMetaLeaderboard = isMetaLeaderboard;
        this.isRegattaLeaderboard = isRegattaLeaderboard;
    } 

    public boolean isMetaLeaderboard() {
        return isMetaLeaderboard;
    }

    public boolean isRegattaLeaderboard() {
        return isRegattaLeaderboard;
    }

    public boolean isFlexibleLeaderboard() {
        return !isRegattaLeaderboard;
    }

    public String toString() {
        String result = "";
        if (isMetaLeaderboard) {
            result += "Meta, ";
        }
        result += isRegattaLeaderboard ? "Regatta" + (this==RegattaLeaderboardWithEliminations?" w/ Elimination":"") : "Flexible";
        return result;
    }
}
