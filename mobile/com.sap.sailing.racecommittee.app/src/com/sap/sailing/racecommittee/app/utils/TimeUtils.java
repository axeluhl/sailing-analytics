package com.sap.sailing.racecommittee.app.utils;

import java.util.Date;

import com.sap.sailing.domain.common.TimePoint;

public class TimeUtils {
	
	public static long timeUntil(TimePoint targetTime) {
		return targetTime.asMillis() - new Date().getTime();
	}
	
	public static CharSequence prettyString(long milliseconds) {
		int secondsTillStart = (int) (milliseconds / 1000);
		int hours = secondsTillStart / 3600;
		int minutes = (secondsTillStart % 3600) / 60;
		int seconds = (secondsTillStart % 60);
		String timePattern = "%s:%s:%s";
		String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
		String minutesString = minutes < 10 ? "0" + minutes : "" + minutes;
		String hoursString = hours < 10 ? "0" + hours : "" + hours;
		return String.format(timePattern, hoursString, minutesString,
				secondsString);
	}
}
