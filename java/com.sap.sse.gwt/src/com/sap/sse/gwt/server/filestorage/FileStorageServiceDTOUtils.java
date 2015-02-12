package com.sap.sse.gwt.server.filestorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServiceProperty;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.gwt.shared.filestorage.FileStorageServiceDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyErrorsDTO;

public class FileStorageServiceDTOUtils {    
    public static FileStorageServicePropertyDTO convert(FileStorageServiceProperty p, Locale locale) {
        return new FileStorageServicePropertyDTO(p.isRequired(), p.getName(), p.getValue(), p.getDescription(locale));
    }
    
    public static FileStorageServicePropertyErrorsDTO convert(InvalidPropertiesException e, Locale locale) {
        Map<FileStorageServicePropertyDTO, String> msgs = new HashMap<>();
        for (Entry<FileStorageServiceProperty, String> entry : e.getPerPropertyMessage().entrySet()) {
            msgs.put(convert(entry.getKey(), locale), entry.getValue());
        }
        return new FileStorageServicePropertyErrorsDTO(e.getMessage(), msgs);
    }
    
    public static FileStorageServiceDTO convert(FileStorageService s, Locale locale) {
        List<FileStorageServicePropertyDTO> pDtos = new ArrayList<>();
        for (FileStorageServiceProperty p : s.getProperties()) {
            pDtos.add(convert(p, locale));
        }
        return new FileStorageServiceDTO(s.getName(), s.getDescription(locale), pDtos.toArray(new FileStorageServicePropertyDTO[0]));
    }
}
