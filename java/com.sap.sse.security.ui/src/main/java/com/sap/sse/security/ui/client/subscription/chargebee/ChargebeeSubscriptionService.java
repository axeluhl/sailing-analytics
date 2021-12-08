package com.sap.sse.security.ui.client.subscription.chargebee;

import com.sap.sse.security.ui.client.subscription.SubscriptionService;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

public interface ChargebeeSubscriptionService
        extends SubscriptionService<ChargebeeConfigurationDTO, PrepareCheckoutDTO> {

}