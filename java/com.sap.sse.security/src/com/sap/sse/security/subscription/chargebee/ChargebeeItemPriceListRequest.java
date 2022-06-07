package com.sap.sse.security.subscription.chargebee;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.models.ItemPrice;
import com.chargebee.models.ItemPrice.ItemPriceListRequest;
import com.chargebee.models.enums.ItemType;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Fetch user subscription list page (by offset), as well as each subscription invoice and transaction data. Results
 * will be notified by {@code ChargebeeSubscriptionListRequest.OnResultListener}
 */
public class ChargebeeItemPriceListRequest extends ChargebeeApiRequest {

    private static final Logger logger = Logger.getLogger(ChargebeeItemPriceListRequest.class.getName());

    @FunctionalInterface
    public static interface OnResultListener {
        void onItemPriceResult(Map<String, BigDecimal> itemPrices, String nextOffset);
    }

    private final OnResultListener listener;
    private final String offset;
    private Map<String, BigDecimal> itemPrices;
    private String nextOffset;
    private int resultSize;

    public ChargebeeItemPriceListRequest(String offset, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor, SubscriptionApiBaseService chargebeeApiServiceParams) {
        super(requestProcessor, chargebeeApiServiceParams);
        this.offset = offset;
        this.listener = listener;
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        logger.info(() -> "Fetch Chargebee subscription itemprices");
        ItemPriceListRequest request = ItemPrice.list().limit(100).itemType().is(ItemType.PLAN);
        if (offset != null && !offset.isEmpty()) {
            request.offset(offset);
        }
        return new ChargebeeInternalApiRequestWrapper(request);
    }

    @Override
    protected void processResult(ChargebeeInternalApiRequestWrapper request) {
        ListResult result = request.getListResult();
        if (result != null) {
            resultSize = result.size();
            nextOffset = result.nextOffset();
            processListResult(result);
        } else {
            onDone(null, null);
        }
    }

    @Override
    protected void handleError(Exception e) {
        logger.log(Level.SEVERE, "Fetching subscription list failed, offset: "
                + (offset == null ? "" : offset), e);
        onDone(null, null);
    }

    private void processListResult(ListResult result) {
        if(itemPrices == null) {
            itemPrices = new HashMap<>();
        }
        for (ListResult.Entry entry : result) {
            final ItemPrice itemPrice = entry.itemPrice();
            // This does only support "flat fee" and "per unit" pricing strategies.
            final BigDecimal priceInDecimal = new BigDecimal(itemPrice.priceInDecimal());
            itemPrices.put(itemPrice.itemId(), priceInDecimal);
        }
        if (itemPrices.size() == resultSize) {
            onDone(itemPrices, nextOffset);
        }
    }

    private void onDone(Map<String, BigDecimal> itemPrices, String nextOffset) {
        if (listener != null) {
            listener.onItemPriceResult(itemPrices, nextOffset);
        }
    }
}
