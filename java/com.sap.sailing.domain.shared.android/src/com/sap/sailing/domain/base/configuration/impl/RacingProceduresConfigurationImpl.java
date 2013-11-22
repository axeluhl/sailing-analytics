package com.sap.sailing.domain.base.configuration.impl;

import java.util.List;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public class RacingProceduresConfigurationImpl implements RacingProceduresConfiguration {

    private static final long serialVersionUID = 8501755084811977792L;
    
    private Flags classFlag;
    private Boolean hasInidividualRecall;
    
    private List<Flags> startModeFlags;
    
    // GENERAL

    @Override
    public Flags getClassFlag() {
        return classFlag;
    }
    
    public void setClassFlag(Flags classFlag) {
        this.classFlag = classFlag;
    }

    @Override
    public Boolean hasInidividualRecall() {
        return hasInidividualRecall;
    }

    public void setHasInidividualRecall(Boolean hasInidividualRecall) {
        this.hasInidividualRecall = hasInidividualRecall;
    }
    
    // RRS26

    @Override
    public List<Flags> getStartModeFlags() {
        return startModeFlags;
    }
    
    public void setStartModeFlags(List<Flags> flags) {
        this.startModeFlags = flags;
    }

    protected RacingProceduresConfiguration copy() {
        RacingProceduresConfigurationImpl copy = new RacingProceduresConfigurationImpl();
        copy.setClassFlag(classFlag);
        copy.setHasInidividualRecall(hasInidividualRecall);
        copy.setStartModeFlags(startModeFlags);
        return copy;
    }

}
