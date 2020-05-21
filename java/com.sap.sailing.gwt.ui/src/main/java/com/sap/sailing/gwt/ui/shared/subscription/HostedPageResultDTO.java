package com.sap.sailing.gwt.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionService;

/**
 * Data transfer object of generating hosted page object {@link SubscriptionService#generateHostedPageObject(String)}
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
