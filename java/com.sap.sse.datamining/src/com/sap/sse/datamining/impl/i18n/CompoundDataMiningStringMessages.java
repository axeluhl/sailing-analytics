package com.sap.sse.datamining.impl.i18n;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class CompoundDataMiningStringMessages implements DataMiningStringMessages {
    
    private static final Logger LOGGER = Logger.getLogger(CompoundDataMiningStringMessages.class.getSimpleName());
    
    private final Collection<DataMiningStringMessages> stringMessages;
    
    public CompoundDataMiningStringMessages() {
        stringMessages = new HashSet<>();
    }

    public CompoundDataMiningStringMessages(Collection<DataMiningStringMessages> stringMessages) {
        this.stringMessages = new HashSet<>(stringMessages);
    }
    
    public void addStringMessages(DataMiningStringMessages stringMessages) {
        this.stringMessages.add(stringMessages);
    }
    
    public void removeStringMessages(DataMiningStringMessages stringMessages) {
        this.stringMessages.remove(stringMessages);
    }
    
    @Override
    public String get(Locale locale, String messageKey) {
        return get(locale, messageKey, new String[0]);
    }
    
    @Override
    public String get(Locale locale, String messageKey, String... parameters) {
        Map<String, String> messages = new HashMap<>();
        for (DataMiningStringMessages stringMessages : this.stringMessages) {
            messages.put(stringMessages.getResourceBaseName(), getFrom(stringMessages, locale, messageKey, parameters));
        }
        return getBestMessageAndLogConflicts(messageKey, messages);
    }

    private String getFrom(DataMiningStringMessages stringMessages, Locale locale, String messageKey, String... parameters) {
        try {
            return stringMessages.get(locale, messageKey, parameters);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private String getBestMessageAndLogConflicts(String messageKey, Map<String, String> messages) throws MissingResourceException {
        String bestMessageResourceBaseName = null;
        String bestMessage = null;
        for (Entry<String, String> messagesEntry : messages.entrySet()) {
            String message = messagesEntry.getValue();
            if (message != null) {
                if (bestMessage == null) {
                    bestMessage = message;
                    bestMessageResourceBaseName = messagesEntry.getKey();
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
            Entry<String, String> messagesEntry) {
        String messageResourceBaseName = messagesEntry.getKey();
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
