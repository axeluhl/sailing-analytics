package com.sap.sailing.xrr.resultimport.impl;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class XRRParserUtil {
    public static TimePoint calculateTimePointForRegattaResults(RegattaResults regattaResult) {
        XMLGregorianCalendar date = regattaResult.getDate();
        XMLGregorianCalendar time = regattaResult.getTime();
        
        date.setHour(time.getHour());
        date.setMinute(time.getMinute());
        date.setSecond(time.getSecond());
        date.setMillisecond(time.getMillisecond());
        
        // setting the timezone (if exist)
        if(date.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
           // if the date field does not contain a timezone we try to get it from the time
            if(time.getTimezone() != DatatypeConstants.FIELD_UNDEFINED) {
                date.setTimezone(time.getTimezone());
            } else {
                // fallback is to take the UTC
                date.setTimezone(0);
            }
        }

        TimePoint timePoint = new MillisecondsTimePoint(date.toGregorianCalendar().getTime());
        return timePoint;
    }

}
