package com.sap.sailing.gwt.ui.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LeaderboardRowDAO implements IsSerializable {
    public CompetitorDAO competitor;
    public Map<String, LeaderboardEntryDAO> fieldsByRaceName;
    public Integer carriedPoints;
}
