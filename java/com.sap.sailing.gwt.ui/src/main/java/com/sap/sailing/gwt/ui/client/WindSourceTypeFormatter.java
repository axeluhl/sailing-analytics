package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;

public class WindSourceTypeFormatter {
    public static String format(WindSourceType windSourceType, StringMessages stringMessages) {
        switch (windSourceType) {
        case COMBINED:
            return stringMessages.combinedWindSourceTypeName();
        case LEG_MIDDLE:
            return stringMessages.legMiddleWindSourceTypeName();
        case COURSE_BASED:
            return stringMessages.courseBasedWindSourceTypeName();
        case TRACK_BASED_ESTIMATION:
            return stringMessages.trackBasedEstimationWindSourceTypeName();
        case EXPEDITION:
            return stringMessages.windSensorWindSourceTypeName();
        case WEB:
            return stringMessages.webWindSourceTypeName();
        case RACECOMMITTEE:
            return stringMessages.raceCommitteeWindSourceTypeName();
        case WINDFINDER:
            return stringMessages.windFinderWindSourceTypeName();
        }
        return null;
    }
    
    public static String tooltipFor(WindSourceType windSourceType, StringMessages stringMessages) {
        switch (windSourceType) {
        case COMBINED:
            return stringMessages.combinedWindSourceTypeTooltip();
        case LEG_MIDDLE:
            return stringMessages.legMiddleWindSourceTypeTooltip();
        case COURSE_BASED:
            return stringMessages.courseBasedWindSourceTypeTooltip();
        case TRACK_BASED_ESTIMATION:
            return stringMessages.trackBasedEstimationWindSourceTypeTooltip();
        case EXPEDITION:
            return stringMessages.windSensorWindSourceTypeTooltip();
        case WEB:
            return stringMessages.webWindSourceTypeTooltip();
        case RACECOMMITTEE:
            return stringMessages.raceCommitteeWindSourceTypeTooltip();
        case WINDFINDER:
            return stringMessages.windFinderWindSourceTypeTooltip();
        }
        return null;
    }
    
    public static String format(WindSource windSource, StringMessages stringMessages) {
        return format(windSource.getType(), stringMessages)+(windSource.getId() == null ? "" : (" "+windSource.getId().toString()));
    }
}
