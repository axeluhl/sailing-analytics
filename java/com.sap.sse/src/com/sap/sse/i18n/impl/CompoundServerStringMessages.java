package com.sap.sse.i18n.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.i18n.ServerStringMessages;

public class CompoundServerStringMessages implements ServerStringMessages {
    
    private static final Logger LOGGER = Logger.getLogger(CompoundServerStringMessages.class.getSimpleName());
    
    private final Collection<ServerStringMessages> stringMessages;
    
    public CompoundServerStringMessages() {
        stringMessages = new HashSet<>();
    }

    public CompoundServerStringMessages(Collection<ServerStringMessages> stringMessages) {
        this.stringMessages = new HashSet<>(stringMessages);
    }
    
    public void addStringMessages(ServerStringMessages stringMessages) {
        this.stringMessages.add(stringMessages);
    }
    
    public void removeStringMessages(ServerStringMessages stringMessages) {
        this.stringMessages.remove(stringMessages);
    }
    
    @Override
    public String get(Locale locale, String messageKey) {
        return get(locale, messageKey, new String[0]);
    }
    
    @Override
    public String get(Locale locale, String messageKey, String... parameters) {
        Map<ServerStringMessages, String> messages = new HashMap<>();
        for (ServerStringMessages stringMessages : this.stringMessages) {
            try {
                messages.put(stringMessages, stringMessages.get(locale, messageKey, parameters));
            } catch (MissingResourceException e) {
            }
        }
        return getBestMessageAndLogConflicts(messageKey, messages);
    }

    private String getBestMessageAndLogConflicts(String messageKey, Map<ServerStringMessages, String> messages) throws MissingResourceException {
        String bestMessageResourceBaseName = null;
        String bestMessage = null;
        for (Entry<ServerStringMessages, String> messagesEntry : messages.entrySet()) {
            String message = messagesEntry.getValue();
            if (message != null) {
                if (bestMessage == null) {
                    bestMessage = message;
                    bestMessageResourceBaseName = messagesEntry.getKey().getResourceBaseName();
                } else {
                    logPossibleConflicts(messageKey, bestMessageResourceBaseName, bestMessage, messagesEntry);
                }
            }
        }
        
        if (bestMessage == null) {
            throw new MissingResourceException("Can't find message for bundle " + this.getClass().getName() + ", key '"
                    + messageKey + "'", this.getClass().getName(), messageKey);
        }
        return bestMessage;
    }

    private void logPossibleConflicts(String messageKey, String bestMessageResourceBaseName, String bestMessage,
            Entry<ServerStringMessages, String> messagesEntry) {
        String messageResourceBaseName = messagesEntry.getKey().getResourceBaseName();
        String message = messagesEntry.getValue();
        
        if (bestMessage.equals(message)) {
            LOGGER.log(Level.INFO, "The resources " + bestMessageResourceBaseName + 
                                   " and " + messageResourceBaseName + " contain the same message for the key '" +
                                    messageKey + "'");
        } else {
            LOGGER.log(Level.INFO, "The resources " + bestMessageResourceBaseName + 
                                   " and " + messageResourceBaseName + " contain different messages for the key '" +
                                    messageKey + "'");
        }
    }

    @Override
    public String getResourceBaseName() {
        return "This implementations has multiple resource bundles";
    }

}
