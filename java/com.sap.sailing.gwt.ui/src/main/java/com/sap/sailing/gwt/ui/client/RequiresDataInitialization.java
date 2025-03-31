package com.sap.sailing.gwt.ui.client;

/**
 * This is used by UI-Components, which require asynchronous data initialization (for example RaceMap, Charts or the
 * Leaderboard)
 * 
 * @author Lennart Hensler (D054527)
 * 
 */
public interface RequiresDataInitialization {

    public void initializeData(boolean showMapControls, boolean showHeaderPanel);
    public boolean isDataInitialized();
    
}
