package com.sap.sse.security.ui.client.subscription.chargebee;

import com.sap.sse.security.ui.client.subscription.SubscriptionWriteServiceAsync;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

public interface ChargebeeSubscriptionWriteServiceAsync extends ChargebeeSubscriptionServiceAsync,
        SubscriptionWriteServiceAsync<ChargebeeConfigurationDTO, PrepareCheckoutDTO, FinishCheckoutDTO> {
}
