package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

public class TrackedRacesTableWrapper extends TableWrapper<RaceDTO, RefreshableSelectionModel<RaceDTO>> {
    public TrackedRacesTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean multiSelection, boolean enablePager) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager, new EntityIdentityComparator<RaceDTO>() {
            @Override
            public boolean representSameEntity(RaceDTO dto1, RaceDTO dto2) {
                return dto1.getRaceIdentifier().equals(dto2.getRaceIdentifier());
            }

            @Override
            public int hashCode(RaceDTO t) {
                return t.getRaceIdentifier().hashCode();
            }
        });
    }
}
