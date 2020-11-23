package com.sap.sailing.landscape;

import com.sap.sse.landscape.ReleaseRepository;
import com.sap.sse.landscape.impl.ReleaseRepositoryImpl;

public interface SailingReleaseRepository extends ReleaseRepository {
    ReleaseRepository INSTANCE = new ReleaseRepositoryImpl("http://releases.sapsailing.com", "build");
}
