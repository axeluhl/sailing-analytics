package com.sap.sse.security.subscription.chargebee;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.models.ItemPrice;
import com.chargebee.models.ItemPrice.ItemPriceListRequest;
import com.chargebee.models.ItemPrice.Status;
import com.chargebee.models.enums.ItemType;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Fetch user subscription list page (by offset), as well as each subscription invoice and transaction data. Results
 * will be notified by {@code ChargebeeSubscriptionListRequest.OnResultListener}
 */
public class ChargebeeItemPriceListRequest extends ChargebeeApiRequest {

    private static final BigDecimal CENT_CURRENCY_DIVISOR = new BigDecimal(100);
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
        logger.info(() -> "Fetch Chargebee ItemPrices");
        ItemPriceListRequest request = ItemPrice.list().limit(100).itemType().is(ItemType.PLAN).status()
                .is(Status.ACTIVE);
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
        logger.log(Level.SEVERE, "Fetching ItemPrice list failed, offset: "
                + (offset == null ? "" : offset), e);
        onDone(null, null);
    }

    private void processListResult(ListResult result) {
        if(itemPrices == null) {
            itemPrices = new HashMap<>();
        }
        for (ListResult.Entry entry : result) {
            final ItemPrice itemPrice = entry.itemPrice();
            final BigDecimal priceInDecimal = getPriceInDecimal(itemPrice);
            if(priceInDecimal != null) {
                itemPrices.put(itemPrice.id(), priceInDecimal);
            }
        }
        if (itemPrices.size() == resultSize) {
            onDone(itemPrices, nextOffset);
        }
    }

    private BigDecimal getPriceInDecimal(final ItemPrice itemPrice) {
        // This does only support "flat fee" and "per unit" pricing strategies.
        if(itemPrice.priceInDecimal() != null) {
            return new BigDecimal(itemPrice.priceInDecimal());
        }else if(itemPrice.price() != null && "USD".equals(itemPrice.currencyCode()) 
                || "EUR".equals(itemPrice.currencyCode())) {
            // We have to assume that the currency is USD or EUR, 
            // since the integer value of itemprice.price() will be in the smallest unit available to the used currency.
            BigDecimal bigDecimal = new BigDecimal(itemPrice.price()).divide(CENT_CURRENCY_DIVISOR);
            return bigDecimal;
        }else {
            logger.log(Level.SEVERE, "Could not parse ItemPrice. Currency not supported: " + itemPrice.currencyCode());
            return null;
        }
    }

    private void onDone(Map<String, BigDecimal> itemPrices, String nextOffset) {
        if (listener != null) {
            listener.onItemPriceResult(itemPrices, nextOffset);
        }
    }
}
