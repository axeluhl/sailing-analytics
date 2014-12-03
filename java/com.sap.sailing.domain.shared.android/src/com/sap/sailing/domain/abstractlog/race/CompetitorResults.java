package com.sap.sailing.domain.abstractlog.race;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.Util;

public interface CompetitorResults extends List<Util.Triple<Serializable, String, MaxPointsReason>> {

}
