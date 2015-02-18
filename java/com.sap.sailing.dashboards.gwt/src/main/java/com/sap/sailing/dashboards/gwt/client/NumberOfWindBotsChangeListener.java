package com.sap.sailing.dashboards.gwt.client;

import java.util.List;

public interface NumberOfWindBotsChangeListener {
    public void numberOfWindBotsChanged(List<String> windBotIDs, WindBotDataRetrieverProvider windBotDataRetrieverProvider);
}
