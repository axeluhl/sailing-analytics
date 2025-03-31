package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;

public class NameBasedStrippedLeaderboardDTOEntityIdentityComparator implements EntityIdentityComparator<StrippedLeaderboardDTO> {
    @Override
    public boolean representSameEntity(StrippedLeaderboardDTO dto1,
            StrippedLeaderboardDTO dto2) {
        return dto1.getName().equals(dto2.getName());
    }

    @Override
    public int hashCode(StrippedLeaderboardDTO t) {
        return t.getName().hashCode();
    }
}
