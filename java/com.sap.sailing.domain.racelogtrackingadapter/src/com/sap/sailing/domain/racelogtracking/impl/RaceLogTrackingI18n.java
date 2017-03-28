package com.sap.sailing.domain.racelogtracking.impl;

import java.nio.charset.StandardCharsets;

import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;

public class RaceLogTrackingI18n {
    private static final String RESOURCE_BASE_NAME = "stringmessages/TrackingStringMessages";

    public static final ResourceBundleStringMessages STRING_MESSAGES = new ResourceBundleStringMessagesImpl(
            RESOURCE_BASE_NAME, RaceLogTrackingI18n.class.getClassLoader(), StandardCharsets.UTF_8.name());
}
