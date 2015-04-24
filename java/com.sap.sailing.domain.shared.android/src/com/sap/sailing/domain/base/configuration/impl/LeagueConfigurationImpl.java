package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.LeagueConfiguration;

public class LeagueConfigurationImpl extends RacingProcedureConfigurationImpl implements LeagueConfiguration {
    private static final long serialVersionUID = 3625803505541394766L;

    protected LeagueConfigurationImpl copy() {
        LeagueConfigurationImpl copy = (LeagueConfigurationImpl) super.copy(new LeagueConfigurationImpl());
        return copy;
    }
    
    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration update) {
        LeagueConfigurationImpl target = (LeagueConfigurationImpl) super.merge(update);
        return target;
    }

}
