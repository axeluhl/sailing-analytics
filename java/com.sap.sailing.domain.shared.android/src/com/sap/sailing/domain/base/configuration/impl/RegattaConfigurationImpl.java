package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.LeagueConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.Duration;

public class RegattaConfigurationImpl implements RegattaConfiguration {

    private static final long serialVersionUID = 8501755084811977792L;

    private RacingProcedureType defaultRacingProcedureType;
    private CourseDesignerMode defaultCourseDesignerMode;
    private Duration defaultProtestTimeDuration;
    
    private RRS26Configuration rrs26Configuration;
    private SWCStartConfiguration swcStartConfiguration;
    private GateStartConfiguration gateStartConfiguration;
    private ESSConfiguration essConfiguration;
    private LeagueConfiguration leagueConfiguration;
    private RacingProcedureConfiguration basicConfiguration;

    @Override
    public RacingProcedureType getDefaultRacingProcedureType() {
        return defaultRacingProcedureType;
    }

    public void setDefaultRacingProcedureType(RacingProcedureType type) {
        defaultRacingProcedureType = type;
    }

    @Override
    public CourseDesignerMode getDefaultCourseDesignerMode() {
        return defaultCourseDesignerMode;
    }

    public void setDefaultCourseDesignerMode(CourseDesignerMode mode) {
        defaultCourseDesignerMode = mode;
    }

    @Override
    public Duration getDefaultProtestTimeDuration() {
        return defaultProtestTimeDuration;
    }

    public void setDefaultProtestTimeDuration(Duration defaultProtestTimeDuration) {
        this.defaultProtestTimeDuration = defaultProtestTimeDuration;
    }

    @Override
    public RRS26Configuration getRRS26Configuration() {
        return rrs26Configuration;
    }

    public void setRRS26Configuration(RRS26Configuration rrs26Configuration) {
        this.rrs26Configuration = rrs26Configuration;
    }

    @Override
    public SWCStartConfiguration getSWCStartConfiguration() {
        return swcStartConfiguration;
    }

    public void setSWCStartConfiguration(SWCStartConfiguration swcStartConfiguration) {
        this.swcStartConfiguration = swcStartConfiguration;
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
    public LeagueConfiguration getLeagueConfiguration() {
        return leagueConfiguration;
    }

    public void setLeagueConfiguration(LeagueConfiguration leagueConfiguration) {
        this.leagueConfiguration = leagueConfiguration;
    }   

    @Override
    public RacingProcedureConfiguration getBasicConfiguration() {
        return basicConfiguration;
    }     
    
    public void setBasicConfiguration(RacingProcedureConfiguration basicConfiguration) {
        this.basicConfiguration = basicConfiguration;
    }   
    
    @Override
    public RegattaConfiguration clone() {
        RegattaConfigurationImpl copy = new RegattaConfigurationImpl();
        copy.setDefaultRacingProcedureType(defaultRacingProcedureType);
        copy.setDefaultCourseDesignerMode(defaultCourseDesignerMode);
        copy.setDefaultProtestTimeDuration(defaultProtestTimeDuration);
        copy.setRRS26Configuration(rrs26Configuration);
        copy.setSWCStartConfiguration(swcStartConfiguration);
        copy.setGateStartConfiguration(gateStartConfiguration);
        copy.setESSConfiguration(essConfiguration);
        copy.setBasicConfiguration(basicConfiguration);
        copy.setLeagueConfiguration(leagueConfiguration);
        return copy;
    }

    @Override
    public RegattaConfiguration merge(RegattaConfiguration update) {
        RegattaConfigurationImpl target = (RegattaConfigurationImpl) this.clone();

        if (update.getDefaultCourseDesignerMode() != null) {
            target.setDefaultCourseDesignerMode(update.getDefaultCourseDesignerMode());
        }
        if (update.getDefaultRacingProcedureType() != null) {
            target.setDefaultRacingProcedureType(update.getDefaultRacingProcedureType());
        }
        if (update.getDefaultProtestTimeDuration() != null) {
            target.setDefaultProtestTimeDuration(update.getDefaultProtestTimeDuration());
        }
        if (update.getRRS26Configuration() != null) {
            target.setRRS26Configuration(
                    (RRS26Configuration) target.getRRS26Configuration().merge(update.getRRS26Configuration()));
        }
        if (update.getSWCStartConfiguration() != null) {
            target.setSWCStartConfiguration(
                    (SWCStartConfiguration) target.getSWCStartConfiguration().merge(update.getSWCStartConfiguration()));
        }
        if (update.getGateStartConfiguration() != null) {
            target.setGateStartConfiguration(
                    (GateStartConfiguration) target.getGateStartConfiguration().merge(update.getGateStartConfiguration()));
        }
        if (update.getESSConfiguration() != null) {
            target.setESSConfiguration(
                    (ESSConfiguration) target.getESSConfiguration().merge(update.getESSConfiguration()));
        }
        if (update.getBasicConfiguration() != null) {
            target.setBasicConfiguration(
                    (RacingProcedureConfiguration) target.getBasicConfiguration().merge(update.getBasicConfiguration()));
        }
        if (update.getLeagueConfiguration() != null) {
            target.setLeagueConfiguration(
                    (LeagueConfiguration) target.getLeagueConfiguration().merge(update.getLeagueConfiguration()));
        }

        return target;
    }
}
