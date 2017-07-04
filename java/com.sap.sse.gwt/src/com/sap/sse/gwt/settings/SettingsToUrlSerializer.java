package com.sap.sse.gwt.settings;

import java.util.Map;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.common.settings.serializer.SettingsToStringMapSerializer;

/**
 * Serializes {@link GenericSerializableSettings} objects to the URL by constructing flat keys from the hierarchical object graph.
 * 
 * This type of serialization has some limitations:
 * <ul>
 * <li>Empty collections can not be serialized. This can only occur for collection settings that have their default value set to be non-empty and there is no actual value.</li>
 * <li>Null values can not be distinguished from empty strings</li>
 * </ul>
 *
 */
public class SettingsToUrlSerializer {
    
    private final SettingsToStringMapSerializer settingsToStringMapSerializer = new SettingsToStringMapSerializer();
    
    public final String serializeBasedOnCurrentLocation(GenericSerializableSettings settings) {
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        serializeToUrlBuilder(settings, urlBuilder);
        return urlBuilder.buildString();
    }
    
    public final String serializeBasedOnCurrentLocationWithCleanParameters(GenericSerializableSettings settings) {
        final UrlBuilder urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParameters();
        serializeToUrlBuilder(settings, urlBuilder);
        return urlBuilder.buildString();
    }
    
    public final UrlBuilder serializeUrlBuilderBasedOnCurrentLocation(GenericSerializableSettings settings) {
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        serializeToUrlBuilder(settings, urlBuilder);
        return urlBuilder;
    }
    
    public final UrlBuilder serializeUrlBuilderBasedOnCurrentLocationWithCleanParameters(GenericSerializableSettings settings) {
        final UrlBuilder urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParameters();
        serializeToUrlBuilder(settings, urlBuilder);
        return urlBuilder;
    }

    public final void serializeToUrlBuilder(GenericSerializableSettings settings, UrlBuilder urlBuilder) {
        Map<String, Iterable<String>> serializedValues = settingsToStringMapSerializer.serialize(settings);
        writeParameterMapToUrlBuilder(urlBuilder, serializedValues);
    }
    
    public final void serializeSettingsMapToUrlBuilder(SettingsMap settings, UrlBuilder urlBuilder) {
        Map<String, Iterable<String>> serializedValues = settingsToStringMapSerializer.serialize(settings);
        writeParameterMapToUrlBuilder(urlBuilder, serializedValues);
    }

    private void writeParameterMapToUrlBuilder(UrlBuilder urlBuilder, Map<String, Iterable<String>> serializedValues) {
        for(Map.Entry<String, Iterable<String>> entry : serializedValues.entrySet()) {
            Iterable<String> parameterValues = entry.getValue();
            String[] parameterValuesAsArray = Util.toArray(parameterValues, new String[Util.size(parameterValues)]);
            if (parameterValuesAsArray.length > 0) {
                urlBuilder.setParameter(entry.getKey(), parameterValuesAsArray);
            }
        }
    }
    
    public final <T extends GenericSerializableSettings> T deserializeFromCurrentLocation(T settings) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<String, Iterable<String>> values = (Map) Window.Location.getParameterMap();
        return settingsToStringMapSerializer.deserialize(settings, values);
    }
    
    public final <T extends SettingsMap> T deserializeSettingsMapFromCurrentLocation(T settings) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<String, Iterable<String>> values = (Map) Window.Location.getParameterMap();
        return settingsToStringMapSerializer.deserializeSettingsMap(settings, values);
    }

}
