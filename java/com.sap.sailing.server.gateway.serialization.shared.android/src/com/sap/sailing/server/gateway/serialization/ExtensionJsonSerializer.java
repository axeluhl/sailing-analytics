package com.sap.sailing.server.gateway.serialization;



public abstract class ExtensionJsonSerializer<ParentType, ExtensionType> {

    public abstract String getExtensionFieldName();

    public abstract Object serializeExtension(ParentType parent);

    protected JsonSerializer<ExtensionType> extensionSerializer;

    public ExtensionJsonSerializer(JsonSerializer<ExtensionType> extensionSerializer) {
        this.extensionSerializer = extensionSerializer;
    }

    protected Object serialize(ExtensionType extensionObject) {
        return extensionSerializer.serialize(extensionObject);
    }

}
