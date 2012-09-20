package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.impl.Util.Pair;

public abstract class SimpleAbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = -3590156714385187908L;

    @Override
    public Pair<Competitor, RaceColumn> getKey(Competitor competitor) {
        return new Pair<Competitor, RaceColumn>(competitor, this);
    }
}
