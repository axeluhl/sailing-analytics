package com.sap.sailing.gwt.ui.shared.racemap;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.PathDTO;

public class PathNameFormatter {
    
    private final StringMessages stringMessages;
    
    public PathNameFormatter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    public String format(PathDTO path) {
        switch (path.getPathType()) {
            case OMNISCIENT: return stringMessages.omniscient();
            case OPPORTUNIST_LEFT: return stringMessages.opportunistLeft();
            case OPPORTUNIST_RIGHT: return stringMessages.opportunistRight();
            case ONE_TURNER_LEFT: return stringMessages.oneTurnerLeft();
            case ONE_TURNER_RIGHT: return stringMessages.oneTurnerRight();
        }
        // fallback, if no path type is specified
        return path.getName();
    }

}
