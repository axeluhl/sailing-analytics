package com.sap.sse.gwt.settings;

import com.google.gwt.http.client.UrlBuilder;
import com.sap.sse.common.settings.Settings;

public abstract class AbstractEntryPointWithSettingsLinkFactory {
    
    private static final SettingsToUrlSerializer serializer = new SettingsToUrlSerializer();

    protected static String createEntryPointLink(String baseLink, Settings... settings) {
        UrlBuilder urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParameters(baseLink);
        for (Settings setting : settings) {
            serializer.serializeToUrlBuilder(setting, urlBuilder);
        }
        return urlBuilder.buildString();
    }
}
