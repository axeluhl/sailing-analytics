package com.sap.sse.datamining.impl.functions;


public class AdditionalMethodWrappingFunctionData {
    
    private final String messageKey;
    private final int resultDecimals;
    private final int ordinal;
    
    public AdditionalMethodWrappingFunctionData(String messageKey, int resultValueDecimals, int ordinal) {
        this.messageKey = messageKey;
        this.resultDecimals = resultValueDecimals;
        this.ordinal = ordinal;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public int getResultDecimals() {
        return resultDecimals;
    }
    
    public int getOrdinal() {
        return ordinal;
    }

}
