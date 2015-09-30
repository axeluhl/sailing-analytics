package com.sap.sailing.dashboards.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;

/**
 * @author Alexander Ries (D062114)
 *
 */
public enum StartlineAdvantageCalculationBasis implements IsSerializable {
    MEASURED,
    
    /**
     * When the {@link StartLineAdvantageDTO} is calculated with the Polar API and there are not values returned
     * there are used manually entered polar values to provide still a {@link StartLineAdvantageDTO}.
     */
    DEFAULT_VALUES    
}
