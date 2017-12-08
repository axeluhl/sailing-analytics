package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.DataResource;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractStatisticsBoxPresenter {
    private NumberFormat simpleFormat = NumberFormat.getFormat("#0.0");
    
    private final StatisticsBoxView view;

    public AbstractStatisticsBoxPresenter(StatisticsBoxView view) {
        this.view = view;
    }

    public void addCompetitorItem(DataResource icon, String name, SimpleCompetitorDTO competitor) {
        if (competitor != null) {
            addItem(icon, name, competitor.getShortInfo() != null ? competitor.getShortInfo() : competitor.getName());
        }
    }
    
    public void addKnotsItem(DataResource icon, String name, Double speedInKnots) {
        if (speedInKnots != null) {
            addItem(icon, name, StringMessages.INSTANCE.knotsValue(speedInKnots));
        }
    }
    
    public void addItemWithCompactFormat(DataResource icon, String name, Double payload) {
        if (payload != null && payload != 0) {
            addItem(icon, name, compactNumber(payload));
        }
    }
    
    public void addItemWithCompactFormat(DataResource icon, String name, long payload) {
        if (payload != 0) {
            addItem(icon, name, compactNumber(payload));
        }
    }

    public void addItemIfNotNull(DataResource icon, String name, Object payload) {
        if (payload != null || payload instanceof Number && ((Number) payload).longValue() != 0) {
            addItem(icon, name, payload);
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
    
    protected void addItem(DataResource icon, String name, Object payload) {
        addItem(icon.getSafeUri().asString(), name, payload);
    }
    
    protected void addItem(String iconUrl, String name, Object payload) {
        view.addItem(iconUrl, name, payload);
    }
}
