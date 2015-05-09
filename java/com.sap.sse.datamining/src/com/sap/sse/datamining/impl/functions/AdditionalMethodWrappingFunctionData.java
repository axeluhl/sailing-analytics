package com.sap.sse.datamining.impl.functions;

import com.sap.sse.datamining.shared.data.Unit;

public class AdditionalMethodWrappingFunctionData {
    
    private final String messageKey;
    private final Unit resultUnit;
    private final int resultDecimals;
    private final int ordinal;
    
    public AdditionalMethodWrappingFunctionData(String messageKey, Unit resultUnit, int resultValueDecimals, int ordinal) {
        this.messageKey = messageKey;
        this.resultUnit = resultUnit;
        this.resultDecimals = resultValueDecimals;
        this.ordinal = ordinal;
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
    
    public int getOrdinal() {
        return ordinal;
    }

}
