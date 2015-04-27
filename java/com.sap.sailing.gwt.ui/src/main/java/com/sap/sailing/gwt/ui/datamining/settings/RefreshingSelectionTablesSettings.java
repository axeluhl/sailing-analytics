package com.sap.sailing.gwt.ui.datamining.settings;

import com.sap.sse.common.settings.AbstractSettings;

public class RefreshingSelectionTablesSettings extends AbstractSettings {
    
    private boolean refreshAutomatically;
    private Integer refreshIntervalInMilliseconds;
    private boolean rerunQueryAfterRefresh;
    
    public RefreshingSelectionTablesSettings() {
        this(true, 5000, false);
    }
    
    public RefreshingSelectionTablesSettings(boolean refreshAutomatically, Integer refreshRateInMilliseconds, boolean rerunQueryAfterRefresh) {
        this.refreshAutomatically = refreshAutomatically;
        this.refreshIntervalInMilliseconds = refreshRateInMilliseconds;
        this.rerunQueryAfterRefresh = rerunQueryAfterRefresh;
    }

    public boolean isRefreshAutomatically() {
        return refreshAutomatically;
    }

    protected void setRefreshAutomatically(boolean refreshAutomatically) {
        this.refreshAutomatically = refreshAutomatically;
    }

    public Integer getRefreshIntervalInMilliseconds() {
        return refreshIntervalInMilliseconds;
    }

    protected void setRefreshIntervalInMilliseconds(int refreshRateInMilliseconds) {
        this.refreshIntervalInMilliseconds = refreshRateInMilliseconds;
    }

    public boolean isRerunQueryAfterRefresh() {
        return rerunQueryAfterRefresh;
    }

    protected void setRerunQueryAfterRefresh(boolean rerunQueryAfterRefresh) {
        this.rerunQueryAfterRefresh = rerunQueryAfterRefresh;
    }

}
