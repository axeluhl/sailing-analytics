package com.sap.sse.security.operations;

import java.math.BigDecimal;
import java.util.Map;

import com.sap.sse.security.impl.ReplicableSecurityService;

/**
 * Update item price operation
 * 
 */
public class UpdateItemPriceOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 4943500215851172841L;

    Map<String, BigDecimal> itemPrices;

    public UpdateItemPriceOperation(Map<String, BigDecimal> itemPrices) {
        this.itemPrices = itemPrices;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalUpdateSubscriptionPlanPrices(itemPrices);
        return null;
    }
}
