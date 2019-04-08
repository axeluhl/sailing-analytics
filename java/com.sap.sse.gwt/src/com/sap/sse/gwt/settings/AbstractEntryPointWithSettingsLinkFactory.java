package com.sap.sse.gwt.settings;

import com.google.gwt.http.client.UrlBuilder;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;

public abstract class AbstractEntryPointWithSettingsLinkFactory {
    
    private static final SettingsToUrlSerializer serializer = new SettingsToUrlSerializer();

    protected static String createEntryPointLink(String path, GenericSerializableSettings... settings) {
        UrlBuilder urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(path);
        for (GenericSerializableSettings setting : settings) {
            serializer.serializeToUrlBuilder(setting, urlBuilder);
        }
        return urlBuilder.buildString();
    }
}
