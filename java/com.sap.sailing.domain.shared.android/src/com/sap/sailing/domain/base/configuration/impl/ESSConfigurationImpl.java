package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;

public class ESSConfigurationImpl extends RacingProcedureConfigurationImpl implements ESSConfiguration {

    private static final long serialVersionUID = 5406098147408723197L;

    protected ESSConfiguration copy() {
        ESSConfigurationImpl copy = (ESSConfigurationImpl) super.copy(new ESSConfigurationImpl());
        return copy;
    }
    
    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration update) {
        ESSConfigurationImpl target = (ESSConfigurationImpl) super.merge(update);
        return target;
    }

}
