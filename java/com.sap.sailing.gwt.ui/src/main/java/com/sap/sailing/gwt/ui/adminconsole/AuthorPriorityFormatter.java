package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.racelog.AuthorPriority;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class AuthorPriorityFormatter {
    public static String getDescription(AuthorPriority authorPriority, StringMessages stringMessages) {
        switch (authorPriority) {
        case ADMIN:
            return stringMessages.administration();
        case OFFICER_ON_VESSEL:
            return stringMessages.raceOfficerOnVessel();
        case SHORE_CONTROL:
            return stringMessages.shoreControl();
        case DEMO_MODE:
            return stringMessages.demoMode();
        }
        throw new RuntimeException("Internal error: don't know author priority "+authorPriority.name());
    }

}
