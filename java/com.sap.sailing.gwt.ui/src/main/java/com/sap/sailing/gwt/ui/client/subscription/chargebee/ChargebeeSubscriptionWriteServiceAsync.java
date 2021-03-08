package com.sap.sailing.gwt.ui.client.subscription.chargebee;

import com.sap.sailing.gwt.ui.client.subscription.SubscriptionWriteServiceAsync;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

public interface ChargebeeSubscriptionWriteServiceAsync extends ChargebeeSubscriptionServiceAsync,
        SubscriptionWriteServiceAsync<ChargebeeConfigurationDTO, PrepareCheckoutDTO, FinishCheckoutDTO> {
}
