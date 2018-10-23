package com.sap.sailing.server.tagging;

import com.sap.sailing.server.RacingEventService;

public class TaggingServiceFactoryImpl implements TaggingServiceFactory {

    @Override
    public TaggingService getService(RacingEventService racingService) {
        return new TaggingServiceImpl(racingService);
    }
}
