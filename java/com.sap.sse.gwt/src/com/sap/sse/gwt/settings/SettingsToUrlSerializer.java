package com.sap.sse.gwt.settings;

import java.util.Map;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.serializer.SettingsToStringMapSerializer;

/**
 * Serializes {@link Settings} objects to the URL by constructing flat keys from the hierarchical object graph.
 * 
 * This type of serialzation has some limitations:
 * <ul>
 * <li>Empty collections can not be serialized. This can only occur for collection settings that have their default value set to be non-empty and there is no actual value.</li>
 * <li>Null values can not be distinguished from empty strings</li>
 * </ul>
 *
 */
public class SettingsToUrlSerializer {
    
    private final SettingsToStringMapSerializer settingsToStringMapSerializer = new SettingsToStringMapSerializer();
    
    public String serializeBasedOnCurrentLocation(Settings settings) {
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        serializeToUrlBuilder(settings, urlBuilder);
        return urlBuilder.buildString();
    }
    
    public String serializeBasedOnCurrentLocationWithCleanParameters(Settings settings) {
        final UrlBuilder urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParameters();
        serializeToUrlBuilder(settings, urlBuilder);
        return urlBuilder.buildString();
    }
    
    public UrlBuilder serializeUrlBuilderBasedOnCurrentLocation(Settings settings) {
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        serializeToUrlBuilder(settings, urlBuilder);
        return urlBuilder;
    }
    
    public UrlBuilder serializeUrlBuilderBasedOnCurrentLocationWithCleanParameters(Settings settings) {
        final UrlBuilder urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParameters();
        serializeToUrlBuilder(settings, urlBuilder);
        return urlBuilder;
    }

    public void serializeToUrlBuilder(Settings settings, UrlBuilder urlBuilder) {
        Map<String, Iterable<String>> serializedValues = settingsToStringMapSerializer.serialize(settings);
        for(Map.Entry<String, Iterable<String>> entry : serializedValues.entrySet()) {
            Iterable<String> parameterValues = entry.getValue();
            String[] parameterValuesAsArray = Util.toArray(parameterValues, new String[Util.size(parameterValues)]);
            if (parameterValuesAsArray.length > 0) {
                urlBuilder.setParameter(entry.getKey(), parameterValuesAsArray);
            }
        }
    }
    
    public <T extends Settings> T deserializeFromCurrentLocation(T settings) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<String, Iterable<String>> values = (Map) Window.Location.getParameterMap();
        return settingsToStringMapSerializer.deserialize(settings, values);
    }

}
