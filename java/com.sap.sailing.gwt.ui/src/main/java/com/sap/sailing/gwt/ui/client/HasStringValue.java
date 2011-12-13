package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public interface HasStringValue {
    /**
     * This method returns the value of a column to render in MinMaxRenderer. 
     * If the value does not exist or is empty for <code>null</code> is returned.
     * @param object
     * @return the value of the Column
     */
    String getStringValueToRender(LeaderboardRowDAO object);
}
