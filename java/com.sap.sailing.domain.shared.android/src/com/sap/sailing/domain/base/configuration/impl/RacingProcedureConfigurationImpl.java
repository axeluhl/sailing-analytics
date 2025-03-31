package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public class RacingProcedureConfigurationImpl implements RacingProcedureConfiguration {

    private static final long serialVersionUID = 8501755084811977792L;
    
    private Flags classFlag;
    
    /**
     * Can a user trigger an individual recall by using the X-Ray flag?
     */
    private Boolean hasIndividualRecall;
    
    /**
     * May/shall the result entry control be used to capture results? If not, only the
     * photo feature for the hardcopy will be availble. Otherwise, a rank editor is offered
     * in the app which submits the score updates which then will be applied to the leaderboard
     * immediately.
     */
    private Boolean isResultEntryEnabled;
    
    @Override
    public Flags getClassFlag() {
        return classFlag;
    }
    
    public void setClassFlag(Flags classFlag) {
        this.classFlag = classFlag;
    }

    @Override
    public Boolean hasIndividualRecall() {
        return hasIndividualRecall;
    }

    public void setHasIndividualRecall(Boolean hasInidividualRecall) {
        this.hasIndividualRecall = hasInidividualRecall;
    }

    @Override
    public Boolean isResultEntryEnabled() {
        return isResultEntryEnabled;
    }

    public void setResultEntryEnabled(Boolean isResultEntryEnabled) {
        this.isResultEntryEnabled = isResultEntryEnabled;
    }

    protected RacingProcedureConfigurationImpl newInstance() {
        return new RacingProcedureConfigurationImpl();
    }
    
    protected RacingProcedureConfiguration copy() {
        return copy(newInstance());
    }
    
    protected RacingProcedureConfiguration copy(RacingProcedureConfigurationImpl target) {
        target.setClassFlag(this.getClassFlag());
        target.setHasIndividualRecall(this.hasIndividualRecall());
        target.setResultEntryEnabled(this.isResultEntryEnabled());
        return target;
    }

    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration update) {
        RacingProcedureConfigurationImpl target = (RacingProcedureConfigurationImpl) this.copy();
        if (update.getClassFlag() != null) {
            target.setClassFlag(update.getClassFlag());
        }
        if (update.hasIndividualRecall() != null) {
            target.setHasIndividualRecall(update.hasIndividualRecall());
        }
        if (update.isResultEntryEnabled() != null) {
            target.setResultEntryEnabled(update.isResultEntryEnabled());
        }
        return target;
    }

}
