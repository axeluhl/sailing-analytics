package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureFactory;

public class RacingProcedureFactoryImpl extends ReadonlyRacingProcedureFactory implements RacingProcedureFactory {

    private final RaceLogEventAuthor author;
    private final RaceLogEventFactory factory;
    
    public RacingProcedureFactoryImpl(RaceLogEventAuthor author, RaceLogEventFactory factory, 
            StoredRacingProceduresConfiguration configuration) {
        super(configuration);
        this.author = author;
        this.factory = factory;
    }
    
    @Override
    protected RaceLogEventAuthor getAuthor() {
        return author;
    }
    
    @Override
    protected RaceLogEventFactory getEventFactory() {
        return factory;
    }
}
