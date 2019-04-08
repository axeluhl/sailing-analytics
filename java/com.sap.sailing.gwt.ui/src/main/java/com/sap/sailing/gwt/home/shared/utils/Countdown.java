package com.sap.sailing.gwt.home.shared.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * A countdown notifying about changes in the two most significant time units. I.e. days+hours or hours+minutes or minutes+seconds or seconds only.
 * If more than WHOLE_DAYS_LIMIT are left, suppress hours.
 * If only seconds are left, only provide seconds.
 */
public class Countdown {
    
    private static final int WHOLE_DAYS_LIMIT = 3;
    private static final int UPDATE_INTERVALL_MILLIS = 1000;
    private final Timer timer;
    private final TimePoint zeroDate;
    private final CountdownListener countdownListener;

    private RemainingTime previousMajor;
    private RemainingTime previousMinor;

    public enum Unit {DAYS, HOURS, MINUTES, SECONDS}
    
    public static class RemainingTime {
        private static StringMessages i18n = GWT.create(StringMessages.class);
        public RemainingTime(long value, Unit unit) {
            this.value = value;
            this.unit = unit;
        }
        public final long value;
        public final Unit unit;
        
        public String unitI18n() {
            if (value == 1) {
                switch (unit) {
                case DAYS:
                    return i18n.countdownDay();
                case HOURS:
                    return i18n.countdownHour();
                case MINUTES:
                    return i18n.countdownMinute();
                case SECONDS:
                    return i18n.countdownSecond();
                default:
                    return "UNKNOWN";

                }
            } else {
                switch (unit) {
                case DAYS:
                    return i18n.countdownDays();
                case HOURS:
                    return i18n.countdownHours();
                case MINUTES:
                    return i18n.countdownMinutes();
                case SECONDS:
                    return i18n.countdownSeconds();
                default:
                    return "UNKNOWN";
                }

            }
        }

        public static boolean equal(RemainingTime t1, RemainingTime t2) {
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
        /**
         * 
         * @param major Whole remaining days, hours or seconds
         * @param minor Remainder measured in the next lower unit. May be null if more than WHOLE_DAYS_LIMIT days are left or if only seconds are left.
         */
        void changed(RemainingTime major, RemainingTime minor);
    }
    
    /**
     * Constructor for testing purpose only. Allows to suppress starting of scheduler (which doesn't work on Java environment).
     * @param zeroDate
     * @param countdownListener
     */
    public Countdown(TimePoint zeroDate, CountdownListener countdownListener, boolean dontSchedule) {
        this.zeroDate = zeroDate;
        this.countdownListener = countdownListener;
        update();
        timer = new Timer() {

            @Override
            public void run() {
                update();
            }
            
        };
        if (!dontSchedule) {
            timer.scheduleRepeating(UPDATE_INTERVALL_MILLIS);
        }
    }
    
    public Countdown(TimePoint zeroDate, CountdownListener countdownListener) {
        this(zeroDate, countdownListener, false);
    }
    
    private void update() {
        Duration diff = MillisecondsTimePoint.now().until(zeroDate);
        if (diff.asMillis() < 0) {
            diff = new MillisecondsDurationImpl(0);
        }
        double asDays = diff.asDays();
        long wholeDaysRemaining = (long) asDays;
        double asHours = diff.asHours();
        long wholeHoursRemaining = (long) (asHours - wholeDaysRemaining * 24.0);
        double asMinutes = diff.asMinutes();
        long wholeMinutesRemaining = (long) (asMinutes - wholeHoursRemaining * 60.0);
        double asSeconds = diff.asSeconds();
        long wholeSecondsRemaining = (long) (asSeconds - wholeMinutesRemaining * 60.0);

        RemainingTime major;
        RemainingTime minor;
        if (wholeDaysRemaining > 0) {
            major = new RemainingTime(wholeDaysRemaining, Unit.DAYS);
            if (wholeDaysRemaining < WHOLE_DAYS_LIMIT) { //show hours only if less than 3 days left
                minor = new RemainingTime(wholeHoursRemaining, Unit.HOURS);
            } else {
                minor = null;
            }
        } else if (wholeHoursRemaining > 0) {
            major = new RemainingTime(wholeHoursRemaining, Unit.HOURS);
            minor = new RemainingTime(wholeMinutesRemaining, Unit.MINUTES);
        }
        else if (wholeMinutesRemaining > 0) {
            major = new RemainingTime(wholeMinutesRemaining, Unit.MINUTES);
            minor = new RemainingTime(wholeSecondsRemaining, Unit.SECONDS);
        } else {
            major = new RemainingTime(wholeSecondsRemaining, Unit.SECONDS);
            minor = null;
        }
        if (!RemainingTime.equal(previousMajor, major) || !RemainingTime.equal(previousMinor, minor)) {
            countdownListener.changed(major, minor);
            previousMajor = major;
            previousMinor = minor;
        }
    }

    public void cancel() {
        timer.cancel();
    }

}
