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
        case EXPEDITION:
            return stringMessages.expeditionWindSourceTypeName();
        case WEB:
            return stringMessages.webWindSourceTypeName();
        }
        return null;
    }
    
    public static String format(WindSource windSource, StringMessages stringMessages) {
        return format(windSource.getType(), stringMessages)+(windSource.getId() == null ? "" : (" "+windSource.getId().toString()));
    }
}
