package com.sap.sailing.selenium.pages.common;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateHelper {
    
    public static Date getPastDate(int daysInPast) {
        return getOffsetDate(-daysInPast, Calendar.DATE);
    }
    
    public static Date getPastTime(int minutesInPast) {
        return getOffsetDate(-minutesInPast, Calendar.MINUTE);
    }
    
    public static Date getCurrentDate() {
        return getOffsetDate(0, Calendar.MILLISECOND);
    }
    
    public static Date getFutureDate(int daysInFuture) {
        return getOffsetDate(daysInFuture, Calendar.DATE);
    }
    
    public static Date getFutureTime(int minutesInFuture) {
        return getOffsetDate(minutesInFuture, Calendar.MINUTE);
    }
    
    private static Date getOffsetDate(int offset, int calendarFieldNumber) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(calendarFieldNumber, offset);
        return calendar.getTime();
    }
    
}
