package com.sap.sse.security.subscription.chargebee;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

public class ChargebeeFetchItemPricesTask implements ChargebeeItemPriceListRequest.OnResultListener {
    private static final Logger logger = Logger.getLogger(ChargebeeFetchItemPricesTask.class.getName());

    @FunctionalInterface
    public static interface OnResultListener {
        void onItemPriceResult(Map<String, BigDecimal> itemPrices);
    }

    private final SubscriptionApiRequestProcessor requestProcessor;
    private final OnResultListener listener;

    private Map<String, BigDecimal> itemPrices;
    private final SubscriptionApiBaseService chargebeeApiServiceParams;

    public ChargebeeFetchItemPricesTask(SubscriptionApiRequestProcessor requestProcessor,
            OnResultListener listener, SubscriptionApiBaseService chargebeeApiServiceParams) {
        this.requestProcessor = requestProcessor;
        this.listener = listener;
        this.chargebeeApiServiceParams = chargebeeApiServiceParams;
    }

    public void run() {
        fetchSubscriptionList(null);
    }

    @Override
    public void onItemPriceResult(Map<String, BigDecimal> itemPrices, String nextOffset) {
        this.itemPrices = itemPrices;
        if (nextOffset == null || nextOffset.isEmpty()) {
            onDone();
        } else {
            fetchSubscriptionList(nextOffset);
        }
    }

    private void fetchSubscriptionList(String offset) {
        logger.info(() -> "Schedule fetch Chargebee subscriptions, offset: "
                + (offset == null ? "" : offset));
        requestProcessor.addRequest(new ChargebeeItemPriceListRequest(offset, this, requestProcessor, chargebeeApiServiceParams));
    }

    private void onDone() {
        if (listener != null) {
            listener.onItemPriceResult(itemPrices);
        }
    }
}
