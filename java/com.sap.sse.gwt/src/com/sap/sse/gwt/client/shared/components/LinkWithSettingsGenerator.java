package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.http.client.UrlBuilder;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.gwt.settings.UrlBuilderUtil;

public class LinkWithSettingsGenerator<S extends Settings> {

    private final SettingsToUrlSerializer settingsToUrlSerializer = new SettingsToUrlSerializer();

    private final String path;
    private final GenericSerializableSettings contextDefinition;

    public LinkWithSettingsGenerator(String path, GenericSerializableSettings contextDefinition) {
        this.path = path;
        this.contextDefinition = contextDefinition;
    }

    public String createUrl(S settings) {
        final UrlBuilder urlBuilder;
        if (path == null) {
            urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParameters();
        } else {
            urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(path);
        }
        serializeSettingsToUrlBuilder(urlBuilder, settings, contextDefinition);
        return urlBuilder.buildString();
    }

    protected void serializeSettingsToUrlBuilder(UrlBuilder urlBuilder, S settings,
            GenericSerializableSettings contextDefinition) {
        if (contextDefinition != null) {
            settingsToUrlSerializer.serializeToUrlBuilder(contextDefinition, urlBuilder);
        }
        if (settings instanceof SettingsMap) {
            settingsToUrlSerializer.serializeSettingsMapToUrlBuilder((SettingsMap) settings, urlBuilder);
        } else if (settings instanceof GenericSerializableSettings) {
            settingsToUrlSerializer.serializeToUrlBuilder((GenericSerializableSettings) settings, urlBuilder);
        }
    }
}
