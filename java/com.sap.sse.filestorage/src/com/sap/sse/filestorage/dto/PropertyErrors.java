package com.sap.sse.filestorage.dto;

import java.io.Serializable;
import java.util.Map;

public class PropertyErrors implements Serializable {
    private static final long serialVersionUID = -7328897153875728802L;
    public Map<PropertyDTO, String> perPropertyMessages;
    public String message;

    // for GWT
    PropertyErrors() {
    }

    public PropertyErrors(String message, Map<PropertyDTO, String> perPropertyMessages) {
        this.message = message;
        this.perPropertyMessages = perPropertyMessages;
    }
}
