package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractStatisticsBoxPresenter {
    private NumberFormat simpleFormat = NumberFormat.getFormat("#0.0");
    
    private final StatisticsBoxView view;

    public AbstractStatisticsBoxPresenter(StatisticsBoxView view) {
        this.view = view;
    }

    public void addCompetitorItem(String iconUrl, String name, SimpleCompetitorDTO competitor) {
        if (competitor != null) {
            addItem(iconUrl, name, competitor.getShortInfo() != null ? competitor.getShortInfo() : competitor.getName());
        }
    }
    
    public void addKnotsItem(String iconUrl, String name, Double speedInKnots) {
        if (speedInKnots != null) {
            addItem(iconUrl, name, StringMessages.INSTANCE.knotsValue(speedInKnots));
        }
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
    
    protected void clear() {
        view.clear();
    }
    
    protected void addItem(String iconUrl, String name, Object payload) {
        view.addItem(iconUrl, name, payload);
    }
}
