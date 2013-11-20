package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.StoreableConfiguration;

/**
 * Is empty, does nothing;
 */
public class EmptyConfiguration extends RacingProceduresConfigurationImpl implements
        StoreableConfiguration<RacingProceduresConfiguration> {

    private static final long serialVersionUID = -4187341706420504456L;

    @Override
    public RacingProceduresConfiguration load() {
        return super.copy(this);
    }

    @Override
    public void store() {
        
    }

}
