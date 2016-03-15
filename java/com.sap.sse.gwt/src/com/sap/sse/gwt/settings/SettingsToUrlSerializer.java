package com.sap.sse.gwt.settings;

import java.util.Map;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.serializer.SettingsToStringMapSerializer;

public class SettingsToUrlSerializer {
    
    private final SettingsToStringMapSerializer settingsToStringMapSerializer = new SettingsToStringMapSerializer();
    
    public void serializeBasedOnCurrentLocation(Settings settings) {
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        serializeToUrlBuilder(settings, urlBuilder);
    }
    
    public void serializeToUrlBuilder(Settings settings, UrlBuilder urlBuilder) {
        Map<String, Iterable<String>> serializedValues = settingsToStringMapSerializer.serialize(settings);
        for(Map.Entry<String, Iterable<String>> entry : serializedValues.entrySet()) {
            Iterable<String> parameterValues = entry.getValue();
            String[] parameterValuesAsArray = Util.toArray(parameterValues, new String[Util.size(parameterValues)]);
            urlBuilder.setParameter(entry.getKey(), parameterValuesAsArray);
        }
    }
    
    public void deserializeFromCurrentLocation(Settings settings) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<String, Iterable<String>> values = (Map) Window.Location.getParameterMap();
        settingsToStringMapSerializer.deserialize(settings, values);
    }

}
