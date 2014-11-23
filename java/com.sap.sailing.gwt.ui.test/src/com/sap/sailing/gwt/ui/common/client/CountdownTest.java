package com.sap.sailing.gwt.ui.common.client;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.sap.sailing.gwt.home.client.shared.Countdown;
import com.sap.sailing.gwt.home.client.shared.Countdown.CountdownListener;
import com.sap.sailing.gwt.home.client.shared.Countdown.RemainingTime;
import com.sap.sailing.gwt.home.client.shared.Countdown.Unit;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CountdownTest {
    
    @Test
    public void testDaysAndHours() throws Exception {
        final long days = 2;
        final long hours = 13;
        final long minutes = 28;
        final long seconds = 2;
        TimePoint zeroDate = MillisecondsTimePoint.now().plus(Duration.ONE_DAY.times(days)).plus(Duration.ONE_HOUR.times(hours)).plus(Duration.ONE_MINUTE.times(minutes)).plus(Duration.ONE_SECOND.times(seconds));
        CountdownListener countdownListener = createCountDownAssertionListener(days, Unit.DAYS, hours, Unit.HOURS);
        new Countdown(zeroDate, countdownListener, true);
    }

    private CountdownListener createCountDownAssertionListener(final long majorValue, final Unit majorUnit, final long minorValue, final Unit minorUnit) {
        return new CountdownListener() {
            
            @Override
            public void changed(RemainingTime major, RemainingTime minor) {
                assertThat(major, is(notNullValue()));
                assertThat(major.value, is(majorValue)); 
                assertThat(major.unit, is(majorUnit)); 
                
                if (minorUnit != null) {
                    assertThat(minor, is(notNullValue()));
                    assertThat(minor.value, is(minorValue)); 
                    assertThat(minor.unit, is(minorUnit));
                } else {
                    assertThat(minor, is(nullValue()));
                }
                
                
            }
        };
    }

    @Test
    public void testDaysNoHours() throws Exception {
        final long days = 2;
        final long hours = 0;
        final long minutes = 28;
        final long seconds = 2;
        TimePoint zeroDate = MillisecondsTimePoint.now().plus(Duration.ONE_DAY.times(days)).plus(Duration.ONE_HOUR.times(hours)).plus(Duration.ONE_MINUTE.times(minutes)).plus(Duration.ONE_SECOND.times(seconds));
        CountdownListener countdownListener = createCountDownAssertionListener(days, Unit.DAYS, hours, Unit.HOURS);
        new Countdown(zeroDate, countdownListener, true);
    }

    @Test
    public void testOnlyDays() throws Exception {
        final long days = 3;
        final long hours = 0;
        final long minutes = 28;
        final long seconds = 2;
        TimePoint zeroDate = MillisecondsTimePoint.now().plus(Duration.ONE_DAY.times(days)).plus(Duration.ONE_HOUR.times(hours)).plus(Duration.ONE_MINUTE.times(minutes)).plus(Duration.ONE_SECOND.times(seconds));
        CountdownListener countdownListener = createCountDownAssertionListener(days, Unit.DAYS, 0, null);
        new Countdown(zeroDate, countdownListener, true);
    }

    @Test
    public void testHoursAndMinutes() throws Exception {
        final long days = 0;
        final long hours = 13;
        final long minutes = 28;
        final long seconds = 2;
        TimePoint zeroDate = MillisecondsTimePoint.now().plus(Duration.ONE_DAY.times(days)).plus(Duration.ONE_HOUR.times(hours)).plus(Duration.ONE_MINUTE.times(minutes)).plus(Duration.ONE_SECOND.times(seconds));
        CountdownListener countdownListener = createCountDownAssertionListener(hours, Unit.HOURS, minutes, Unit.MINUTES);
        new Countdown(zeroDate, countdownListener, true);
    }

    @Test
    public void testMinutesAndSeconds() throws Exception {
        final long days = 0;
        final long hours = 0;
        final long minutes = 28;
        final long seconds = 2;
        TimePoint zeroDate = MillisecondsTimePoint.now().plus(Duration.ONE_DAY.times(days)).plus(Duration.ONE_HOUR.times(hours)).plus(Duration.ONE_MINUTE.times(minutes)).plus(Duration.ONE_SECOND.times(seconds));
        CountdownListener countdownListener = createCountDownAssertionListener(minutes, Unit.MINUTES, seconds, Unit.SECONDS);
        new Countdown(zeroDate, countdownListener, true);
    }

    @Test
    public void testSecondOnly() throws Exception {
        final long days = 0;
        final long hours = 0;
        final long minutes = 0;
        final long seconds = 2;
        TimePoint zeroDate = MillisecondsTimePoint.now().plus(Duration.ONE_DAY.times(days)).plus(Duration.ONE_HOUR.times(hours)).plus(Duration.ONE_MINUTE.times(minutes)).plus(Duration.ONE_SECOND.times(seconds));
        CountdownListener countdownListener = createCountDownAssertionListener(seconds, Unit.SECONDS, 0, null);
        new Countdown(zeroDate, countdownListener, true);
    }

}
