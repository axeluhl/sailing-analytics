package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.statistic;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;

public class SailorProfileNumericStatisticTypeFormater {

    private static final DateTimeFormat dateToHoursAndMinutesFormat = DateTimeFormat.getFormat("HH:mm");

    public static String getDisplayName(SailorProfileNumericStatisticType type, StringMessages stringMessages) {
        switch (type) {
        case AVERAGE_STARTLINE_DISTANCE:
            return stringMessages.averageDistanceToStartLine();
        case BEST_DISTANCE_TO_START:
            return stringMessages.shortestDistanceToStartline();
        case BEST_STARTLINE_SPEED:
            return stringMessages.maxStartLineSpeed();
        case MAX_SPEED:
            return stringMessages.maxSpeedTitle();
        default:
            return type.name();
        }
    }

    public static String format(TimePoint timePoint) {
        return dateToHoursAndMinutesFormat.format(timePoint.asDate());
    }

    public static String format(SailorProfileNumericStatisticType type, Double value, StringMessages stringMessages) {
        // FIXME
        return "" + value;
    }

}
