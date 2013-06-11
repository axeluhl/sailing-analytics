package com.sap.sailing.domain.common.media;

import java.util.Date;

public class MediaUtil {

    public static int compareDatesAllowingNull(Date date1, Date date2) {
        if (date1 == null) {
            return date2 == null ? 0 : -1;
        } else if (date2 == null) {
            return 1;
        } else {
            return date1.compareTo(date2);
        }
    }

    public static boolean equalsDatesAllowingNull(Date date1, Date date2) {
        if (date1 == null) {
            return date2 == null;
        } else if (date2 == null) {
            return false;
        } else {
            return date1.equals(date2);
        }
    }

}
