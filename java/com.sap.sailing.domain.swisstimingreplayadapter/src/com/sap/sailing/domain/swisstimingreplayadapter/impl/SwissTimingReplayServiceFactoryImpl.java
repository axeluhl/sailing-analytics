package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;

public class SwissTimingReplayServiceFactoryImpl implements SwissTimingReplayServiceFactory {

    @Override
    public SwissTimingReplayService createSwissTimingReplayService() {
        return new SwissTimingReplayServiceImpl();
    }

}
