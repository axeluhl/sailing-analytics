package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public class RacingProcedureConfigurationImpl implements RacingProcedureConfiguration {

    private static final long serialVersionUID = 8501755084811977792L;
    
    private Flags classFlag;
    private Boolean hasInidividualRecall;@Override
    
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

    protected RacingProcedureConfiguration copy() {
        return copy(new RacingProcedureConfigurationImpl());
    }
    
    protected RacingProcedureConfiguration copy(RacingProcedureConfigurationImpl target) {
        target.setClassFlag(this.getClassFlag());
        target.setHasInidividualRecall(this.hasInidividualRecall());
        return target;
    }

    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration update) {
        RacingProcedureConfigurationImpl target = (RacingProcedureConfigurationImpl) this.copy();
        if (update.getClassFlag() != null) {
            target.setClassFlag(update.getClassFlag());
        }
        if (update.hasInidividualRecall() != null) {
            target.setHasInidividualRecall(update.hasInidividualRecall());
        }
        return target;
    }

}
