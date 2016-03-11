package com.sap.sailing.selenium.pages.common;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateHelper {
    
    public static Date getPastDate(int daysInPast) {
        return getOffsetDate(-daysInPast);
    }
    
    public static Date getCurrentDate() {
        return getOffsetDate(0);
    }
    
    public static Date getFutureDate(int daysInFuture) {
        return getOffsetDate(daysInFuture);
    }
    
    private static Date getOffsetDate(int offsetInDays) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DATE, offsetInDays);
        return calendar.getTime();
    }

}
