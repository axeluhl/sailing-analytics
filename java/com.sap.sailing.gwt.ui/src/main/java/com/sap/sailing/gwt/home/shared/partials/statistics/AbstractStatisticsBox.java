package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventStatisticsDTO;

public abstract class AbstractStatisticsBox extends Composite implements RefreshableWidget<EventStatisticsDTO> {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;
    public static final String ICON_REGATTAS_FOUGHT = "images/mobile/icon_regattasFought.svg";
    public static final String ICON_COMPATITORS_COUNT = "images/mobile/icon_averageSpeed.svg";
    public static final String ICON_RACES_COUNT = "images/mobile/icon_racesCount.svg";
    public static final String ICON_TRACKED_COUNT = "images/mobile/icon_trackedCount.svg";
    public static final String ICON_FASTEST_SAILOR = "images/mobile/fastest_sailor.svg";
    public static final String ICON_RAW_GPS_FIX = "images/mobile/raw_gps_fixes.svg";
    public static final String ICON_WIND_FIX = "images/mobile/strongest_wind.svg";
    public static final String ICON_SUM_MILES = "images/mobile/sum_miles.svg";
    
    private NumberFormat simpleFormat = NumberFormat.getFormat("#0.0");
    
    private final boolean showRegattaInformation;

    public AbstractStatisticsBox(boolean showRegattaInformation) {
        this.showRegattaInformation = showRegattaInformation;
    }

    public void addItemWithCompactFormat(String iconUrl, String name, Double payload) {
        if (payload != null && payload != 0) {
            addItem(iconUrl, name, compactNumber(payload));
        }
    }
    
    public void addItemWithCompactFormat(String iconUrl, String name, long payload) {
        if (payload != 0) {
            addItem(iconUrl, name, compactNumber(payload));
        }
    }

    public void addItemIfNotNull(String iconUrl, String name, Object payload) {
        if (payload != null || payload instanceof Number && ((Number) payload).longValue() != 0) {
            addItem(iconUrl, name, payload);
        }
    }

    @Override
    public void setData(EventStatisticsDTO statistics) {
        clear();
        if (showRegattaInformation) {
            addItem(ICON_REGATTAS_FOUGHT, MSG.regattas(), statistics.getRegattasFoughtCount());
        }
        addItem(ICON_COMPATITORS_COUNT, MSG.competitors(), statistics.getCompetitorsCount());
        addItem(ICON_RACES_COUNT, MSG.races(), statistics.getRacesRunCount());
        addItem(ICON_TRACKED_COUNT, MSG.trackedRaces(), statistics.getTrackedRacesCount());
        addItemWithCompactFormat(ICON_RAW_GPS_FIX, MSG.numberOfGPSFixes(), statistics.getNumberOfGPSFixes());
        addItemWithCompactFormat(ICON_WIND_FIX, MSG.numberWindFixes(), statistics.getNumberOfWindFixes());
        addItemWithCompactFormat(ICON_SUM_MILES, MSG.sailedMiles(), statistics.getTotalDistanceTraveled());
        addItemWithCompactFormat(ICON_FASTEST_SAILOR, MSG.fastestSailor() + ": "
                + statistics.getCompetitorInfo(), statistics.getCompetitorSpeed());
    }
    
    private String compactNumber(double value) {
        if(value < 100.0) {
            return simpleFormat.format(value);
        }
        if(value < 100_000.0) {
            return "" + Double.valueOf(value).intValue();
        }
        if(value < 100_000_000.0) {
            return StringMessages.INSTANCE.millionValue(value / 1_000_000.0);
        }
        return StringMessages.INSTANCE.billionValue(value / 1_000_000_000.0);
    }
    
    private String compactNumber(long value) {
        if(value < 100_000l) {
            return "" + value;
        }
        if(value < 100_000_000l) {
            return StringMessages.INSTANCE.millionValue(value / 1_000_000.0);
        }
        return StringMessages.INSTANCE.billionValue(value / 1_000_000_000.0);
    }
    
    protected abstract void clear();
    
    protected abstract void addItem(String iconUrl, String name, Object payload);

}
