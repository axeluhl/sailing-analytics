package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.statistic;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxResources;
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

    public static String getIcon(SailorProfileNumericStatisticType type) {
        switch (type) {
        case AVERAGE_STARTLINE_DISTANCE:
            return StatisticsBoxResources.INSTANCE.fastestSailorWhite().getSafeUri().asString();
        case BEST_DISTANCE_TO_START:
            return StatisticsBoxResources.INSTANCE.fastestSailorWhite().getSafeUri().asString();
        case BEST_STARTLINE_SPEED:
            return StatisticsBoxResources.INSTANCE.maxSpeedWhite().getSafeUri().asString();
        case MAX_SPEED:
            return StatisticsBoxResources.INSTANCE.maxSpeedWhite().getSafeUri().asString();
        default:
            return "";
        }
    }

    public static String format(TimePoint timePoint) {
        return dateToHoursAndMinutesFormat.format(timePoint.asDate());
    }

    public static String format(SailorProfileNumericStatisticType type, Double value, StringMessages stringMessages) {
        switch (type) {
        case AVERAGE_STARTLINE_DISTANCE:
            return "\u00D8  " + stringMessages.metersValue(value);
        case BEST_DISTANCE_TO_START:
            return stringMessages.metersValue(value);
        case BEST_STARTLINE_SPEED:
        case MAX_SPEED:
            return stringMessages.knotsValue(value);
        default:
            return "" + value;
        }
    }

    public static String getColumnHeadingName(SailorProfileNumericStatisticType type, StringMessages stringMessages) {
        switch (type) {
        case AVERAGE_STARTLINE_DISTANCE:
        case BEST_DISTANCE_TO_START:
            return stringMessages.distance();
        case BEST_STARTLINE_SPEED:
        case MAX_SPEED:
            return stringMessages.speed();
        default:
            return stringMessages.value();
        }
    }

}
