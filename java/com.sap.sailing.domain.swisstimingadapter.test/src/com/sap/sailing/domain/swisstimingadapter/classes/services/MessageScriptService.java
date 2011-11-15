package com.sap.sailing.domain.swisstimingadapter.classes.services;

import java.io.IOException;
import java.util.List;

public interface MessageScriptService {
    void updateMessage(Object message);
    void createMessagePosition(int positoin, Object message);
    void createMessage(Object message);
    void deleteMessage(Object message);
    List<Object> getAllMessages();
    Object getMessageId(int id);
    
    void saveToFile(String path) throws IOException;
    void readFromFile(String path);
}
