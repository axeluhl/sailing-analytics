package com.sap.sailing.domain.swisstimingadapter.classes.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            if(messageList.get(i).equals(message)){
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
            if(messageList.get(i).equals(message)){
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
    public void readFromFile(String path) {
        fileService.readListFromFile(path);
    }

     
}
