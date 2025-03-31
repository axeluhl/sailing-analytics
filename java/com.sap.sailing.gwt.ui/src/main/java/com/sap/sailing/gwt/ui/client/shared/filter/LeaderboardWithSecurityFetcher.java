package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.function.Consumer;

import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public interface LeaderboardWithSecurityFetcher {

    public void getLeaderboardWithSecurity(
            Consumer<StrippedLeaderboardDTO> consumer);
}
