package com.sap.sailing.dashboards.gwt.client.dataretriever;

import java.util.List;

public interface NumberOfWindBotsChangeListener {
    public void numberOfWindBotsChanged(List<String> windBotIDs, WindBotDataRetrieverProvider windBotDataRetrieverProvider);
}
