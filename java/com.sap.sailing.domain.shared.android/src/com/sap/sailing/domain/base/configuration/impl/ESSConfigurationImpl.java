package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;

public class ESSConfigurationImpl extends RacingProcedureConfigurationImpl implements ESSConfiguration {

    private static final long serialVersionUID = 5406098147408723197L;

    protected ESSConfiguration copy() {
        ESSConfigurationImpl copy = (ESSConfigurationImpl) super.copy(new ESSConfigurationImpl());
        return copy;
    }

}
