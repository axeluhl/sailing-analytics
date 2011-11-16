package com.sap.sailing.domain.swisstimingadapter.classes.services.old;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.classes.services.Exceptions.MessageScriptParsingException;

public interface MessageScriptService {
    void updateMessage(Object message);
    void createMessagePosition(int positoin, Object message);
    void createMessage(Object message);
    void deleteMessage(Object message);
    List<Object> getAllMessages();
    Object getMessageId(int id);
    
    void saveToFile(String path) throws IOException;
    void readFromFile(String path) throws IOException, ParseException, MessageScriptParsingException;
}
