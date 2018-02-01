package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.google.gwt.user.client.ui.IsWidget;

public interface StatisticsBoxView extends IsWidget {

    void clear();
    
    void addItem(String iconUrl, String name, Object payload);
}
