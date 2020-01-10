package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;

public class NameBasedStrippedLeaderboardDTOEntityIdentityComparator implements EntityIdentityComparator<StrippedLeaderboardDTOWithSecurity> {
    @Override
    public boolean representSameEntity(StrippedLeaderboardDTOWithSecurity dto1,
            StrippedLeaderboardDTOWithSecurity dto2) {
        return dto1.getName().equals(dto2.getName());
    }

    @Override
    public int hashCode(StrippedLeaderboardDTOWithSecurity t) {
        return t.getName().hashCode();
    }
}
