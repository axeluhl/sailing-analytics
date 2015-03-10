package com.sap.sse.filestorage.impl;

import java.util.Locale;

import com.sap.sse.filestorage.FileStorageServiceProperty;

public class FileStorageServicePropertyImpl implements FileStorageServiceProperty {
    private static final long serialVersionUID = -8418630141746910125L;
    private final boolean isRequired;
    private final String name;
    private final String descriptionKey;
    private String value;
    
    public FileStorageServicePropertyImpl(String name, boolean isRequired, String descriptionKey) {
        this.name = name;
        this.descriptionKey = descriptionKey;
        this.isRequired = isRequired;
    }

    @Override
    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription(Locale locale) {
        return FileStorageI18n.STRING_MESSAGES.get(locale, descriptionKey);
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return name + "=" + value;
    }
}
