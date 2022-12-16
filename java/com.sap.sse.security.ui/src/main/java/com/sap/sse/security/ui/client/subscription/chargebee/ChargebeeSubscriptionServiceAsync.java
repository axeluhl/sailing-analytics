package com.sap.sse.security.ui.client.subscription.chargebee;

import com.sap.sse.security.ui.client.subscription.SubscriptionServiceAsync;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

public interface ChargebeeSubscriptionServiceAsync
        extends SubscriptionServiceAsync<ChargebeeConfigurationDTO, PrepareCheckoutDTO> {
}
