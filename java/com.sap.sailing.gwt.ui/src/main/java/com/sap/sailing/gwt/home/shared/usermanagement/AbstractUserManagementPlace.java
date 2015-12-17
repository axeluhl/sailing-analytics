package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;

public abstract class AbstractUserManagementPlace extends Place implements HasLocationTitle {
    
    private final Place nextTarget;
    
    protected AbstractUserManagementPlace() {
        this(new StartPlace());
    }

    protected AbstractUserManagementPlace(Place nextTarget) {
        this.nextTarget = nextTarget;
    }
    
    public Place getNextTarget() {
        return nextTarget;
    }

}
