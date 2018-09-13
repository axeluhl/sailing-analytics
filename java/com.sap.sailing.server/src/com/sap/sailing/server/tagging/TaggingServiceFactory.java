package com.sap.sailing.server.tagging;

import com.sap.sailing.server.RacingEventService;

public interface TaggingServiceFactory {

    static TaggingServiceFactory INSTANCE = new TaggingServiceFactoryImpl();

    public TaggingService getService(RacingEventService racingService);

}
