package com.sap.sse.security.ui.client.subscription.chargebee;

import com.sap.sse.security.ui.client.subscription.SubscriptionWriteService;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

public interface ChargebeeSubscriptionWriteService
        extends SubscriptionWriteService<ChargebeeConfigurationDTO, PrepareCheckoutDTO, FinishCheckoutDTO> {
}
