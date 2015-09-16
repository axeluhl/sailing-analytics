package com.sap.sse.datamining.test.util.components;

import com.sap.sse.common.settings.SerializableSettings;

public class Test_NullRetrievalProcessorSettings extends SerializableSettings {
    private static final long serialVersionUID = -373296620115384430L;
    
    private final String value;

    public Test_NullRetrievalProcessorSettings(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }

}
