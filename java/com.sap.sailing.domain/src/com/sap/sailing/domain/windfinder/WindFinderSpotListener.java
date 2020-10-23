package com.sap.sailing.domain.windfinder;

import com.sap.sailing.domain.common.Wind;

public interface WindFinderSpotListener {
    void windDataReceived(Iterable<Wind> windFixes, Spot spot);
}
