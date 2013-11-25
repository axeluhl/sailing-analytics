package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;

public class RacingProceduresConfigurationImpl implements RacingProceduresConfiguration {

    private static final long serialVersionUID = 8501755084811977792L;
    
    private RRS26Configuration rrs26Configuration;
    private GateStartConfiguration gateStartConfiguration;
    private ESSConfiguration essConfiguration;
    private RacingProcedureConfiguration basicConfiguration;

    @Override
    public RRS26Configuration getRRS26Configuration() {
        return rrs26Configuration;
    }

    public void setRRS26Configuration(RRS26Configuration rrs26Configuration) {
        this.rrs26Configuration = rrs26Configuration;
    }

    @Override
    public GateStartConfiguration getGateStartConfiguration() {
        return gateStartConfiguration;
    }

    public void setGateStartConfiguration(GateStartConfiguration gateStartConfiguration) {
        this.gateStartConfiguration = gateStartConfiguration;
    }

    @Override
    public ESSConfiguration getESSConfiguration() {
        return essConfiguration;
    }

    public void setESSConfiguration(ESSConfiguration essConfiguration) {
        this.essConfiguration = essConfiguration;
    }   

    @Override
    public RacingProcedureConfiguration getBasicConfiguration() {
        return basicConfiguration;
    }     
    
    public void setBasicConfiguration(RacingProcedureConfiguration basicConfiguration) {
        this.basicConfiguration = basicConfiguration;
    }   
    
    protected RacingProceduresConfiguration copy() {
        RacingProceduresConfigurationImpl copy = new RacingProceduresConfigurationImpl();
        copy.setRRS26Configuration(rrs26Configuration);
        copy.setGateStartConfiguration(gateStartConfiguration);
        copy.setESSConfiguration(essConfiguration);
        copy.setBasicConfiguration(basicConfiguration);
        return copy;
    }

}
