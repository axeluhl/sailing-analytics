package com.sap.sailing.server.tagging;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.TaggingService;

public class TaggingServiceFactoryImpl implements TaggingServiceFactory {

    @Override
    public TaggingService getService(RacingEventService racingService) {
        return new TaggingServiceImpl(racingService);
    }
}
