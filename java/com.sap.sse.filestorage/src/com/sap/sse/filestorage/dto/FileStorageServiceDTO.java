package com.sap.sse.filestorage.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.Property;

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

    public static FileStorageServiceDTO convert(FileStorageService s) {
        List<PropertyDTO> pDtos = new ArrayList<>();
        for (Property p : s.getProperties()) {
            pDtos.add(PropertyDTO.convert(p));
        }
        return new FileStorageServiceDTO(s.getName(), s.getDescription(), pDtos.toArray(new PropertyDTO[0]));
    }
}
