package com.sap.sailing.server.gateway.serialization;

import java.util.Collections;

import org.json.simple.JSONObject;

public abstract class ExtendableJsonSerializer<T> implements JsonSerializer<T> {

    protected abstract JSONObject serializeFields(T object);

    private final Iterable<ExtensionJsonSerializer<T, ?>> extensionSerializers;

    public ExtendableJsonSerializer(Iterable<ExtensionJsonSerializer<T, ?>> extensionSerializers) {
        this.extensionSerializers = extensionSerializers;
    }

    public ExtendableJsonSerializer(ExtensionJsonSerializer<T, ?> extensionSerializer) {
        this.extensionSerializers = Collections.<ExtensionJsonSerializer<T, ?>> singletonList(extensionSerializer);
    }

    public ExtendableJsonSerializer() {
        this.extensionSerializers = Collections.emptyList();
    }

    @Override
    public JSONObject serialize(T object) {
        JSONObject result = serializeFields(object);

        for (ExtensionJsonSerializer<T, ?> extensionSerializer : extensionSerializers) {
            result.put(extensionSerializer.getExtensionFieldName(), extensionSerializer.serializeExtension(object));
        }

        return result;
    }

}
