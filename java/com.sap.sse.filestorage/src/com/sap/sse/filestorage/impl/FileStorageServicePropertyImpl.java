package com.sap.sse.filestorage.impl;

import com.sap.sse.filestorage.common.FileStorageServiceProperty;


public class FileStorageServicePropertyImpl implements FileStorageServiceProperty {
    private final boolean isRequired;
    private final String name;
    private final String description;
    private String value;
    
    public FileStorageServicePropertyImpl(String name, boolean isRequired, String description) {
        this(name, isRequired, description, null);
    }
    
    public FileStorageServicePropertyImpl(String name, boolean isRequired, String description, String value) {
        this.name = name;
        this.description = description;
        this.value = value;
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
    public String getDescription() {
        return description;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
