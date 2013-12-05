package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;

public interface LiveDataListener {
    void fixesReceived(Iterable<Fix> fixes);
}
