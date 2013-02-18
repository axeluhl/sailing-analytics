package com.sap.sailing.domain.base;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.MarkPassing;

public interface DomainFactory extends SharedDomainFactory {
    static DomainFactory INSTANCE = new DomainFactoryImpl();


    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor);
    
    /**
     * When de-serializing objects of types whose instances that are managed and cached by this domain factory,
     * de-serialized instances need to be replaced by / resolved to the counterparts already known by this factory.
     * The stream returned by this method can be used 
     */
    ObjectInputStreamResolvingAgainstDomainFactory createObjectInputStreamResolvingAgainstThisFactory(InputStream inputStream) throws IOException;
    
    ScoringScheme createScoringScheme(ScoringSchemeType scoringSchemeType);

}
