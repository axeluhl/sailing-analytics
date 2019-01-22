package com.sap.sailing.domain.windestimation;

import java.util.function.Consumer;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface WindEstimationFactoryService {
    IncrementalWindEstimationTrack createIncrementalWindEstimationTrack(TrackedRace trackedRace);

    void registerDomainFactory(DomainFactory domainFactory);

    void runWithDomainFactory(Consumer<DomainFactory> consumer) throws InterruptedException;
}
