package com.sap.sailing.gwt.ui.client.subscription.chargebee;

import com.sap.sailing.gwt.ui.client.subscription.SubscriptionWriteService;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

public interface ChargebeeSubscriptionWriteService
        extends SubscriptionWriteService<ChargebeeConfigurationDTO, PrepareCheckoutDTO, FinishCheckoutDTO> {

}
