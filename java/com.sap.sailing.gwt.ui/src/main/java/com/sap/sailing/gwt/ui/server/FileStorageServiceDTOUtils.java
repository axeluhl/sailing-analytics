package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.gwt.ui.shared.FileStorageServicePropertyDTO;
import com.sap.sailing.gwt.ui.shared.FileStorageServicePropertyErrorsDTO;
import com.sap.sailing.gwt.ui.shared.FileStorageServiceDTO;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.FileStorageServiceProperty;

public class FileStorageServiceDTOUtils {    
    public static FileStorageServicePropertyDTO convert(FileStorageServiceProperty p) {
        return new FileStorageServicePropertyDTO(p.isRequired(), p.getName(), p.getValue(), p.getDescription());
    }
    
    public static FileStorageServicePropertyErrorsDTO convert(InvalidPropertiesException e) {
        Map<FileStorageServicePropertyDTO, String> msgs = new HashMap<>();
        for (Entry<FileStorageServiceProperty, String> entry : e.getPerPropertyMessage().entrySet()) {
            msgs.put(convert(entry.getKey()), entry.getValue());
        }
        return new FileStorageServicePropertyErrorsDTO(e.getMessage(), msgs);
    }
    
    public static FileStorageServiceDTO convert(FileStorageService s) {
        List<FileStorageServicePropertyDTO> pDtos = new ArrayList<>();
        for (FileStorageServiceProperty p : s.getProperties()) {
            pDtos.add(convert(p));
        }
        return new FileStorageServiceDTO(s.getName(), s.getDescription(), pDtos.toArray(new FileStorageServicePropertyDTO[0]));
    }
}
