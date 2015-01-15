package com.sap.sailing.domain.common;

import com.sap.sse.common.Duration;

/**
 * Provides methods to format a {@link Duration}.
 * 
 * @author Simon Pamies
 */
public class DurationFormatter {

    private final boolean shortMode;
    private final boolean nonNegativeDuration;
    
    /**
     * Returns a {@link DurationFormatter} that can be used to format {@link Duration} instances.
     *  
     * @param shortMode if set to true then the formatted output won't show labels (instead of 2 hours it will show 2h).
     * @param nonNegativeDuration if set to true the formatter will only show positive values even for negative durations 
     * @return
     */
    public static DurationFormatter getInstance(boolean shortMode, boolean nonNegativeDuration) {
        return new DurationFormatter(shortMode, nonNegativeDuration);
    }
    
    public DurationFormatter(boolean shortMode, boolean nonNegativeDuration) {
        this.shortMode = shortMode;
        this.nonNegativeDuration = nonNegativeDuration;
    }
    
    public String format(Duration instance) {
        StringBuffer result = new StringBuffer();
        double asDays = instance.asDays();
        double asHours = instance.asHours();
        double asMinutes = instance.asMinutes();
        if (asDays >= 1. || asDays <= -1.) {
            result.append((int) ensureDisplay(asDays, nonNegativeDuration) + (shortMode ? "d" : " day"));
            if ((asDays >= 2. || asDays <= -2.) && !shortMode) {
                result.append("s");
            }
        } else if (asHours >= 1. || asHours <= -1.) {
            result.append((int) ensureDisplay(asHours, nonNegativeDuration) + (shortMode ? "h" : " hour"));
            if ((asHours >= 2. || asHours <= -2.) && !shortMode) {
                result.append("s");
            }
        } else if (asMinutes >= 1. || asMinutes <= -1.) {
            int minutesToSee = (int) ensureDisplay(new Double(asMinutes), nonNegativeDuration);
            int secondsToSee = (int) ensureDisplay(instance.minus(Duration.ONE_MINUTE.times((long) Math.abs(asMinutes))).asSeconds(), nonNegativeDuration);
            if (secondsToSee < 10 && secondsToSee > 0) {
                // this is a hack but we can't use NumberFormat here because this
                // class might be used in a context where there is no NumberFormat available
                result.append(minutesToSee + ":0" + secondsToSee + (shortMode ? "" : " min"));
            } else {
                if (secondsToSee < 0) {
                    // never display negative seconds for a combination of minutes and seconds
                    secondsToSee = Math.abs(secondsToSee);
                }
                result.append(minutesToSee + ":" + secondsToSee + (shortMode ? "" : " min"));
            }
        } else {
            result.append((int) ensureDisplay(instance.asSeconds(), nonNegativeDuration));
            if (!shortMode) {
                result.append(" s");
            } 
        }
        return result.toString();
    }
    
    private double ensureDisplay(double input, boolean nonNegativeDurationDisplay) {
        return (nonNegativeDurationDisplay && input < 0) ? Math.abs(input) : input;
    }
    
}
