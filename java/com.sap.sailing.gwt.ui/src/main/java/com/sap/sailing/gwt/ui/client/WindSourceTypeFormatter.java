package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;

public class WindSourceTypeFormatter {
    public static String format(WindSourceType windSourceType, StringMessages stringMessages) {
        switch (windSourceType) {
        case COMBINED:
            return stringMessages.combinedWindSourceTypeName();
        case COURSE_BASED:
            return stringMessages.courseBasedWindSourceTypeName();
        case TRACK_BASED_ESTIMATION:
            return stringMessages.trackBasedEstimationWindSourceTypeName();
        case WIND_SENSOR:
            return stringMessages.windSensorWindSourceTypeName();
        case WEB:
            return stringMessages.webWindSourceTypeName();
        case RACECOMMITTEE:
            return stringMessages.raceCommitteeWindSourceTypeName();
        }
        return null;
    }
    
    public static String tooltipFor(WindSourceType windSourceType, StringMessages stringMessages) {
        switch (windSourceType) {
        case COMBINED:
            return stringMessages.combinedWindSourceTypeTooltip();
        case COURSE_BASED:
            return stringMessages.courseBasedWindSourceTypeTooltip();
        case TRACK_BASED_ESTIMATION:
            return stringMessages.trackBasedEstimationWindSourceTypeTooltip();
        case WIND_SENSOR:
            return stringMessages.windSensorWindSourceTypeTooltip();
        case WEB:
            return stringMessages.webWindSourceTypeTooltip();
        case RACECOMMITTEE:
            return stringMessages.raceCommitteeWindSourceTypeTooltip();
        }
        return null;
    }
    
    public static String format(WindSource windSource, StringMessages stringMessages) {
        return format(windSource.getType(), stringMessages)+(windSource.getId() == null ? "" : (" "+windSource.getId().toString()));
    }
}
