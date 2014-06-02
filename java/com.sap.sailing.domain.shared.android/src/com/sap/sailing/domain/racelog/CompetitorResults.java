package com.sap.sailing.domain.racelog;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.UtilNew;

public interface CompetitorResults extends List<UtilNew.Triple<Serializable, String, MaxPointsReason>> {

}
