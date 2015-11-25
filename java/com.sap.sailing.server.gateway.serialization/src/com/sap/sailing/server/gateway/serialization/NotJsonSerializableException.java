package com.sap.sailing.server.gateway.serialization;

import java.io.NotSerializableException;

public class NotJsonSerializableException extends NotSerializableException {
    private static final long serialVersionUID = 7890086707210316414L;
    
    private Class<?> notSerializableClass;

    public NotJsonSerializableException(Class<?> notSerializableClass) {
        super(notSerializableClass.getName());
        this.notSerializableClass = notSerializableClass;
    }
    
    public Class<?> getNotSerializableClass() {
        return notSerializableClass;
    }
    
}
