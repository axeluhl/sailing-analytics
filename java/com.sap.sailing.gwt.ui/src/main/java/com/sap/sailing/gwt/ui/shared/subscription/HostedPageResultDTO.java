package com.sap.sailing.gwt.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionService;

/**
 * Result object of generating Chargebee hosted page object RPC {@link SubscriptionService}
 * 
 * @author tutran
 */
public class HostedPageResultDTO implements IsSerializable {
    /**
     * In success case, hostedPageJSONString has value of JSON encoded string of hosted page object
     */
    public String hostedPageJSONString;

    /**
     * In fail case, error contains error message
     */
    public String error;
}
