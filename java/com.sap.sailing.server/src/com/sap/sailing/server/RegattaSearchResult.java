package com.sap.sailing.server;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sse.common.search.Hit;

public interface RegattaSearchResult extends Hit {
    Regatta getRegatta();
}
