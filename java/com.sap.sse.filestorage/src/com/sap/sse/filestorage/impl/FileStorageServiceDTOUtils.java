package com.sap.sse.filestorage.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.Property;
import com.sap.sse.filestorage.dto.FileStorageServiceDTO;
import com.sap.sse.filestorage.dto.PropertyDTO;
import com.sap.sse.filestorage.dto.PropertyErrors;

public class FileStorageServiceDTOUtils {    
    public static PropertyDTO convert(Property p) {
        return new PropertyDTO(p.isRequired(), p.getName(), p.getValue(), p.getDescription());
    }
    
    public static PropertyErrors convert(InvalidPropertiesException e) {
        Map<PropertyDTO, String> msgs = new HashMap<>();
        for (Entry<Property, String> entry : e.getPerPropertyMessage().entrySet()) {
            msgs.put(convert(entry.getKey()), entry.getValue());
        }
        return new PropertyErrors(e.getMessage(), msgs);
    }
    
    public static FileStorageServiceDTO convert(FileStorageService s) {
        List<PropertyDTO> pDtos = new ArrayList<>();
        for (Property p : s.getProperties()) {
            pDtos.add(convert(p));
        }
        return new FileStorageServiceDTO(s.getName(), s.getDescription(), pDtos.toArray(new PropertyDTO[0]));
    }
}
