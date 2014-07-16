package com.sap.sailing.gwt.ui.common.client;

import com.google.gwt.user.client.Timer;
import com.sap.sailing.domain.common.Duration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;

/**
 * A countdown notifying about changes in the two most significant time units. I.e. days+hours or hours+minutes or minutes+seconds or seconds only.
 */
public class Countdown {
    
    private static final int UPDATE_INTERVALL_MILLIS = 1000;
    private final Timer timer;
    private final TimePoint zeroDate;
    private final CountdownListener countdownListener;

    private Time previousMajor;
    private Time previousMinor;

    public enum Unit {DAYS, HOURS, MINUTES, SECONDS}
    
    public static class Time {
        public Time(long value, Unit unit) {
            this.value = value;
            this.unit = unit;
        }
        public final long value;
        public final Unit unit;
        
        public static boolean equal(Time t1, Time t2) {
            if (t1 == null) {
                if (t2 == null) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (t2 == null) {
                    return false;
                } else {
                    return t1.value == t2.value && t1.unit == t2.unit;
                }
            }
        }
    }
    
    public interface CountdownListener {
        void changed(Time major, Time minor);
    }
    
    public Countdown(TimePoint zeroDate, CountdownListener countdownListener) {
        this.zeroDate = zeroDate;
        this.countdownListener = countdownListener;
        update();
        timer = new Timer() {

            @Override
            public void run() {
                update();
            }
            
        };
        timer.scheduleRepeating(UPDATE_INTERVALL_MILLIS);
    }
    
    private void update() {
        Duration diff = MillisecondsTimePoint.now().until(zeroDate);
        double asDays = diff.asDays();
        long daysRemaining = (long) asDays;
        double asHours = diff.asHours();
        long hoursRemaining = (long) (asHours - daysRemaining * 24.0);
        double asMinutes = diff.asMinutes();
        long minutesRemaining = (long) (asMinutes - hoursRemaining * 60.0);
        double asSeconds = diff.asSeconds();
        long secondsRemaining = (long) (asSeconds - minutesRemaining * 60.0);

        Time major = null;
        Time minor = null;
        if (daysRemaining > 0) {
            major = new Time(daysRemaining, Unit.DAYS);
        }
        if (hoursRemaining > 0) {
            Time time = new Time(hoursRemaining, Unit.HOURS);
            if (major == null) {
                major = time;
            } else {
                minor = time;
            }
        }
        if (minutesRemaining > 0) {
            Time time = new Time(minutesRemaining, Unit.MINUTES);
            if (major == null) {
                major = time;
            } else if (major.unit == Unit.HOURS) {
                minor = time;
            }
        }
        if (secondsRemaining >= 0) {
            Time time = new Time(secondsRemaining, Unit.SECONDS);
            if (major == null) {
                major = time;
            } else if (major.unit == Unit.MINUTES) {
                minor = time;
            }
        }

        if (!Time.equal(previousMajor, major) || !Time.equal(previousMinor, minor)) {
            countdownListener.changed(major, minor);
            previousMajor = major;
            previousMinor = minor;
        }
    }

    public void cancel() {
        timer.cancel();
    }

}
