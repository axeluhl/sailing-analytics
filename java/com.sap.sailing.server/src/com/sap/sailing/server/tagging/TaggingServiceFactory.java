package com.sap.sailing.server.tagging;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.TaggingService;

public interface TaggingServiceFactory {
    static TaggingServiceFactory INSTANCE = new TaggingServiceFactoryImpl();

    TaggingService getService(RacingEventService racingService);
}
