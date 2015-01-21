package com.sap.sailing.domain.common.media;

import com.sap.sse.common.TimePoint;

public class MediaUtil {

    public static int compareDatesAllowingNull(TimePoint date1, TimePoint date2) {
        if (date1 == null) {
            return date2 == null ? 0 : -1;
        } else if (date2 == null) {
            return 1;
        } else {
            return date1.compareTo(date2);
        }
    }

    public static boolean equalsDatesAllowingNull(TimePoint date1, TimePoint date2) {
        if (date1 == null) {
            return date2 == null;
        } else if (date2 == null) {
            return false;
        } else {
            return date1.equals(date2);
        }
    }

}
