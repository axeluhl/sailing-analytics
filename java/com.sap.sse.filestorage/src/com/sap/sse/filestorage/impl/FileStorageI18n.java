package com.sap.sse.filestorage.impl;

import com.sap.sse.i18n.ServerStringMessages;
import com.sap.sse.i18n.impl.ServerStringMessagesImpl;

public class FileStorageI18n {    
    private static final String RESOURCE_BASE_NAME = "stringmessages/FileStorageStringMessages";
    
    public static final ServerStringMessages STRING_MESSAGES = new ServerStringMessagesImpl(RESOURCE_BASE_NAME, FileStorageI18n.class.getClassLoader());
}
