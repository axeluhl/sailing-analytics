package com.sap.sse.filestorage.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.Property;

public class PropertyErrors implements Serializable {
    private static final long serialVersionUID = -7328897153875728802L;
    public Map<PropertyDTO, String> perPropertyMessages = new HashMap<>();
    public String message;

    // for GWT
    PropertyErrors() {
    }

    public PropertyErrors(InvalidPropertiesException e) {
        message = e.getMessage();
        for (Entry<Property, String> entry : e.getPerPropertyMessage().entrySet()) {
            this.perPropertyMessages.put(PropertyDTO.convert(entry.getKey()), entry.getValue());
        }
    }
}
