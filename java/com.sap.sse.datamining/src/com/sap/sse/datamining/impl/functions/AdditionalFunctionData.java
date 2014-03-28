package com.sap.sse.datamining.impl.functions;

import com.sap.sse.datamining.shared.Unit;

public class AdditionalFunctionData {
    
    private final String messageKey;
    private final Unit resultUnit;
    private final int resultDecimals;
    
    public AdditionalFunctionData(String messageKey, Unit resultUnit, int resultValueDecimals) {
        this.messageKey = messageKey;
        this.resultUnit = resultUnit;
        this.resultDecimals = resultValueDecimals;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Unit getResultUnit() {
        return resultUnit;
    }

    public int getResultDecimals() {
        return resultDecimals;
    }

}
