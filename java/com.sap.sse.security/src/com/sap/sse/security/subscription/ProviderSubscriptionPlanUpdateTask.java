package com.sap.sse.security.subscription;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.security.SecurityService;

/**
 * Perform fetching and updating user subscriptions of a provider service
 */
public class ProviderSubscriptionPlanUpdateTask implements SubscriptionApiService.OnItemPriceResultListener {
    private static final Logger logger = Logger.getLogger(ProviderSubscriptionPlanUpdateTask.class.getName());

    private final SubscriptionApiService apiService;
    private final CompletableFuture<SecurityService> securityService;

    public ProviderSubscriptionPlanUpdateTask(SubscriptionApiService apiService,
            CompletableFuture<SecurityService> securityService) {
        this.apiService = apiService;
        this.securityService = securityService;
    }

    public void run() {
        try {
            // Currently this only retrieves and updates Itemprices. Additional Processes and Updates might be queued here.
            apiService.getItemPrices(this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fetch subscription plans failed, provider: " 
        + apiService.getProviderName(), e);
        }
    }

    @Override
    /**
     * When Item Prices for the respective Provider is received. An update Action is published.
     */
    public void onItemPriceResult(Map<String, BigDecimal> itemPrices) {
        if (itemPrices != null) {
            getSecurityService().updateSubscriptionPlanPrices(itemPrices);
        } else {
            logger.log(Level.SEVERE, "Updating item prices failed, provider: " + apiService.getProviderName());
        }
    }

    private SecurityService getSecurityService() {
        try {
            return securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Failure to get SecurityService", e);
            throw new RuntimeException(e);
        }
    }
}
