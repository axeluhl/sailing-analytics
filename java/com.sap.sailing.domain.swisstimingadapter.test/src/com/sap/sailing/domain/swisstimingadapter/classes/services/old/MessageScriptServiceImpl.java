package com.sap.sailing.domain.swisstimingadapter.classes.services.old;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.classes.services.MessageFileService;
import com.sap.sailing.domain.swisstimingadapter.classes.services.MessageFileServiceImpl;
import com.sap.sailing.domain.swisstimingadapter.classes.services.Exceptions.MessageScriptParsingException;

public class MessageScriptServiceImpl implements MessageScriptService{

    private List<Object> messageList;
    private MessageFileService fileService;
    
    
    public MessageScriptServiceImpl() {
        messageList = new ArrayList<Object>();
        fileService = new MessageFileServiceImpl();
    }

    @Override
    public void updateMessage(Object message) {
        for (int i = 0; i < messageList.size(); i++) {
            // compare per refereence since 16.11.11 11:38
            if(messageList.get(i)==message){
                messageList.remove(i);
                messageList.add(i-1, message);
            }
        }
    }
    
    @Override
    public void createMessage(Object message) {
        messageList.add(message);
    }

    @Override
    public void createMessagePosition(int pos, Object message) {
        messageList.add(pos, message);
    }

    @Override
    public void deleteMessage(Object message) {
        for (int i = 0; i < messageList.size(); i++) {
         // compare per refereence since 16.11.11 11:38
            if(messageList.get(i)==message){
                messageList.remove(i);
            }
        }
    }

    @Override
    public List<Object> getAllMessages() {
        return messageList;
    }

    @Override
    public Object getMessageId(int id) {
        return messageList.get(id);
    }

    @Override
    public void saveToFile(String path) throws IOException {
        fileService.writeListToFile(path, messageList);
    }

    @Override
    public void readFromFile(String path) throws IOException, ParseException, MessageScriptParsingException {
        fileService.readListFromFile(path);
    }

     
}
