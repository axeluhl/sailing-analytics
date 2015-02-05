package com.sap.sse.datamining.test.util;

import com.sap.sse.i18n.ServerStringMessages;
import com.sap.sse.i18n.impl.CompoundServerStringMessages;
import com.sap.sse.i18n.impl.ServerStringMessagesImpl;

public class TestsUtil {
    
    private static final String TEST_STRING_MESSAGES_BASE_NAME = "stringmessages/Test_StringMessages";
    private static ServerStringMessages TEST_STRING_MESSAGES;

    private static final String PRODUCTIVE_STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
    private static CompoundServerStringMessages EXTENDED_STRING_MESSAGES;
    
    public static ServerStringMessages getTestStringMessages() {
        if (TEST_STRING_MESSAGES == null) {
            TEST_STRING_MESSAGES = new ServerStringMessagesImpl(TEST_STRING_MESSAGES_BASE_NAME, TestsUtil.class.getClassLoader());
        }
        
        return TEST_STRING_MESSAGES;
    }
    
    public static ServerStringMessages getTestStringMessagesWithProductiveMessages() {
        if (EXTENDED_STRING_MESSAGES == null) {
            EXTENDED_STRING_MESSAGES = new CompoundServerStringMessages();
            EXTENDED_STRING_MESSAGES.addStringMessages(getTestStringMessages());
            EXTENDED_STRING_MESSAGES.addStringMessages(new ServerStringMessagesImpl(PRODUCTIVE_STRING_MESSAGES_BASE_NAME, TestsUtil.class.getClassLoader()));
        }
        
        return EXTENDED_STRING_MESSAGES;
    }
    
    protected TestsUtil() { }

}
