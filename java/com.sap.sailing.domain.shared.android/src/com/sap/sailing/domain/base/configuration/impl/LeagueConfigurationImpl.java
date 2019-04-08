package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.procedures.LeagueConfiguration;

public class LeagueConfigurationImpl extends RacingProcedureConfigurationImpl implements LeagueConfiguration {
    private static final long serialVersionUID = 3625803505541394766L;

    protected LeagueConfigurationImpl newInstance() {
        return new LeagueConfigurationImpl();
    }
}
