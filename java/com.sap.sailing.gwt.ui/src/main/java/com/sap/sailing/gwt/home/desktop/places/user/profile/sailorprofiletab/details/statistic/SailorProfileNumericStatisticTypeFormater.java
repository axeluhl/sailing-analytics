package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.statistic;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.shared.partials.statistics.StatisticsBoxResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;

/** contains methods for formatting numeric statistics in sailor profiles */
public final class SailorProfileNumericStatisticTypeFormater {

    private SailorProfileNumericStatisticTypeFormater() {
    }

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

    /** @return an icon safe uri corresponding to the given statistic type */
    public static SafeUri getIcon(SailorProfileNumericStatisticType type) {
        switch (type) {
        case AVERAGE_STARTLINE_DISTANCE:
            return StatisticsBoxResources.INSTANCE.fastestSailorWhite().getSafeUri();
        case BEST_DISTANCE_TO_START:
            return StatisticsBoxResources.INSTANCE.fastestSailorWhite().getSafeUri();
        case BEST_STARTLINE_SPEED:
            return StatisticsBoxResources.INSTANCE.maxSpeedWhite().getSafeUri();
        case MAX_SPEED:
            return StatisticsBoxResources.INSTANCE.maxSpeedWhite().getSafeUri();
        default:
            return UriUtils.fromSafeConstant("");
        }
    }

    /** @return the {@link #timePoint} formatted as hours and minutes */
    public static String format(TimePoint timePoint) {
        return dateToHoursAndMinutesFormat.format(timePoint.asDate());
    }

    /** @return the formatted value with unit corresponding to the given statistic type */
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

    /** @return the column heading name corresponding to the given statistic type */
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
