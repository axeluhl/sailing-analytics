package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LeaderboardEntryDAO implements IsSerializable {
    /**
     * Either <code>null</code> in case no max points, or one of "DNS", "DNF", "OCS", "DND", "RAF", "BFD", "DNC", or "DSQ"
     */
    public String reasonForMaxPoints;
    
    public int netPoints;
    
    public int totalPoints;
    
    public boolean discarded;
    
}
