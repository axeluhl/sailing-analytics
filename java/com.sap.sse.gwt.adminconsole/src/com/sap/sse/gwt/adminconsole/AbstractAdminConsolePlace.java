package com.sap.sse.gwt.adminconsole;

import java.util.Map;

import com.sap.sse.gwt.client.AbstractBasePlace;

public abstract class AbstractAdminConsolePlace extends AbstractBasePlace implements AdminConsolePlace {
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