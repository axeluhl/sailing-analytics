package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.Map;

import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sse.gwt.adminconsole.AdminConsolePlace;

public abstract class AbstractAdminConsolePlace extends AbstractBasePlace implements AdminConsolePlace  {
    protected AbstractAdminConsolePlace(String placeParamsFromUrlFragment) {
        super(placeParamsFromUrlFragment);
    }

    protected AbstractAdminConsolePlace(Map<String, String> paramKeysAndValues) {
        super(paramKeysAndValues);
    }

    public boolean isSamePlace(Object obj) {
        return obj != null && obj.getClass() == this.getClass();
    }
}