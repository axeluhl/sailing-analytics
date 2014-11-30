package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.ArrayList;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.Util;

public class CompetitorResultsImpl extends ArrayList<Util.Triple<Serializable, String, MaxPointsReason>> implements CompetitorResults {

    private static final long serialVersionUID = 4928351242700897387L;

}
