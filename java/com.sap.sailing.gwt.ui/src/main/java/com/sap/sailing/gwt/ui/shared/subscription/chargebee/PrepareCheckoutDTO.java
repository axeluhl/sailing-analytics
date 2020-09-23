package com.sap.sailing.gwt.ui.shared.subscription.chargebee;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.ChargebeeSubscriptionService;

/**
 * Data transfer object for preparing checkout {@link ChargebeeSubscriptionService#prepareCheckout}. Data contains
 * hosted page object returned from Chargebee
 * 
 * @author Tu Tran
 */
public class PrepareCheckoutDTO implements IsSerializable {
    /**
     * In success case, hostedPageJSONString has value of JSON encoded string of hosted page object
     */
    private String hostedPageJSONString;

    /**
     * In fail case, error contains error message
     */
    private String error;

    public String getHostedPageJSONString() {
        return hostedPageJSONString;
    }

    public void setHostedPageJSONString(String hostedPageJSONString) {
        this.hostedPageJSONString = hostedPageJSONString;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
