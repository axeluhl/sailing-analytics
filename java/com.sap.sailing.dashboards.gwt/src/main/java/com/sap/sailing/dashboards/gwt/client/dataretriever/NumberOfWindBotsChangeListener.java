package com.sap.sailing.dashboards.gwt.client.dataretriever;

import java.util.List;

public interface NumberOfWindBotsChangeListener {
    public void numberOfWindBotsReceivedChanged(List<String> windBotIDs, WindBotDataRetrieverProvider windBotDataRetrieverProvider);
    public void numberOfWindBotsReceivedIsZero();
}
