package com.sap.sse.filestorage.test.util;

import java.util.Arrays;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServiceProperty;

public class Util {
    public static FileStorageServiceProperty findProperty(FileStorageService service, String name) {
        return Arrays.asList(service.getProperties()).stream().filter(p -> p.getName().equals(name)).findFirst()
                .get();
    }
}
