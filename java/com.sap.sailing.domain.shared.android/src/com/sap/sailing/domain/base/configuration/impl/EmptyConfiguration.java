package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;

public class EmptyConfiguration extends RacingProceduresConfigurationImpl implements
        StoredRacingProceduresConfiguration {

    private static final long serialVersionUID = -4187341706420504456L;

    @Override
    public StoredRacingProceduresConfiguration load() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public StoredRacingProceduresConfiguration store() {
        // TODO Auto-generated method stub
        return this;
    }

}
