package com.sap.sse.filestorage.impl;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServiceProperty;
import com.sap.sse.filestorage.FileStorageServiceResolver;

public abstract class BaseFileStorageServiceImpl implements FileStorageService {
    private static final long serialVersionUID = 7787261863522200165L;
    private final String name;
    private final String descriptionKey;
    protected final Map<String, FileStorageServicePropertyImpl> propertiesByNameInInsertionOrder = new LinkedHashMap<>();
    
    protected BaseFileStorageServiceImpl(String name, String descriptionKey) {
        this.name = name;
        this.descriptionKey = descriptionKey;
    }
    
    protected void addProperties(FileStorageServicePropertyImpl... properties) {
        for (FileStorageServicePropertyImpl p : properties) {
            this.propertiesByNameInInsertionOrder.put(p.getName(), p);
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription(Locale locale) {
        return FileStorageI18n.STRING_MESSAGES.get(locale, descriptionKey);
    }

    @Override
    public IsManagedByCache<FileStorageServiceResolver> resolve(FileStorageServiceResolver domainFactory) {
        return domainFactory.getFileStorageService(getName());
    }
    
    @Override
    public FileStorageServiceProperty[] getProperties() {
        return propertiesByNameInInsertionOrder.values().toArray(new FileStorageServicePropertyImpl[0]);
    }

    @Override
    public void internalSetProperty(String name, String value) {
        if (!propertiesByNameInInsertionOrder.containsKey(name)) {
            throw new IllegalArgumentException("Property " + name + " does not exist");
        }
        propertiesByNameInInsertionOrder.get(name).setValue(value);
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
