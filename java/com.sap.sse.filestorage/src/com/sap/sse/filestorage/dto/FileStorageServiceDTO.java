package com.sap.sse.filestorage.dto;

import java.io.Serializable;

public class FileStorageServiceDTO implements Serializable {
    private static final long serialVersionUID = 6101940297792100418L;
    public String name;
    public String description;
    public PropertyDTO[] properties;

    // for GWT
    FileStorageServiceDTO() {
    }

    public FileStorageServiceDTO(String name, String description, PropertyDTO... properties) {
        this.name = name;
        this.description = description;
        this.properties = properties;
    }
}
