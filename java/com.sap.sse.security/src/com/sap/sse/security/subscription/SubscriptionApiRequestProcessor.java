package com.sap.sse.security.subscription;

public interface SubscriptionApiRequestProcessor {
    void process();
    
    void addRequest(SubscriptionApiRequest request, long delayMs);
}
