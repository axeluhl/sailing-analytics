package com.sap.sse.datamining.test.util;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.i18n.CompoundDataMiningStringMessages;
import com.sap.sse.datamining.impl.i18n.DataMiningStringMessagesImpl;

public class TestsUtil {
    
    private static final String TEST_STRING_MESSAGES_BASE_NAME = "stringmessages/Test_StringMessages";
    private static DataMiningStringMessages TEST_STRING_MESSAGES;

    private static final String PRODUCTIVE_STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
    private static CompoundDataMiningStringMessages EXTENDED_STRING_MESSAGES;
    
    public static DataMiningStringMessages getTestStringMessages() {
        if (TEST_STRING_MESSAGES == null) {
            TEST_STRING_MESSAGES = new DataMiningStringMessagesImpl(TEST_STRING_MESSAGES_BASE_NAME, TestsUtil.class.getClassLoader());
        }
        
        return TEST_STRING_MESSAGES;
    }
    
    public static DataMiningStringMessages getTestStringMessagesWithProductiveMessages() {
        if (EXTENDED_STRING_MESSAGES == null) {
            EXTENDED_STRING_MESSAGES = new CompoundDataMiningStringMessages();
            EXTENDED_STRING_MESSAGES.addStringMessages(getTestStringMessages());
            EXTENDED_STRING_MESSAGES.addStringMessages(new DataMiningStringMessagesImpl(PRODUCTIVE_STRING_MESSAGES_BASE_NAME, TestsUtil.class.getClassLoader()));
        }
        
        return EXTENDED_STRING_MESSAGES;
    }
    
    protected TestsUtil() { }

}
