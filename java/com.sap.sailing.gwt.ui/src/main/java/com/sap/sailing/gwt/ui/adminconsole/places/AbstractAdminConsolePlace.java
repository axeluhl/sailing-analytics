package com.sap.sailing.gwt.ui.adminconsole.places;

import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sse.gwt.adminconsole.AdminConsolePlace;

public abstract class AbstractAdminConsolePlace extends AbstractBasePlace implements AdminConsolePlace  {

    public boolean isSamePlace(Object obj) {
        return obj != null && obj.getClass() == this.getClass();
    }

}