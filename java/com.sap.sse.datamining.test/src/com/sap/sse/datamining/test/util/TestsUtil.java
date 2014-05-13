package com.sap.sse.datamining.test.util;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public class TestsUtil {
    
    private static final String TEST_STRING_MESSAGES_BASE_NAME = "stringmessages/Test_StringMessages";
    private static final DataMiningStringMessages TEST_STRING_MESSAGES = DataMiningStringMessages.Util.getInstanceFor(TEST_STRING_MESSAGES_BASE_NAME);
    private static final DataMiningStringMessages EXTENDED_STRING_MESSAGES = DataMiningStringMessages.Util.getCompoundStringMessages(
                                                                                DataMiningStringMessages.Util.getDefaultStringMessagesBaseName(),
                                                                                TEST_STRING_MESSAGES_BASE_NAME);
    
    public static DataMiningStringMessages getTestStringMessages() {
        return TEST_STRING_MESSAGES;
    }
    
    public static DataMiningStringMessages getTestStringMessagesWithProductiveMessages() {
        return EXTENDED_STRING_MESSAGES;
    }
    
    protected TestsUtil() { }

}
