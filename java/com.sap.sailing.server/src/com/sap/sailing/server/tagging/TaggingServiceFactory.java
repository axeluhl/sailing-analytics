package com.sap.sailing.server.tagging;

import com.sap.sailing.server.RacingEventService;

public interface TaggingServiceFactory {
    static TaggingServiceFactory INSTANCE = new TaggingServiceFactoryImpl();

    TaggingService getService(RacingEventService racingService);
}
