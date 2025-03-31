package com.sap.sailing.gwt.home.communication;

import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

public class SailingSerializationDummyResult implements Result {

    private DegreePosition degreePosition;

    private SailingSerializationDummyResult() {
    }

    public DegreePosition getDegreePosition() {
        return degreePosition;
    }

}
