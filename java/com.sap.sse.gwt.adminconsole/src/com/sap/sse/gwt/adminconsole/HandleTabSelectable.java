package com.sap.sse.gwt.adminconsole;

import java.util.Map;

public interface HandleTabSelectable {
    /**
     * Select tab by names
     * @param verticalTabName - vertical name tab menu
     * @param horizontalTabName - subtab name of vertical tab menu
     * @param params - map of strings parameters
     */
    void selectTabByNames(String verticalTabName, String horizontalTabName, Map<String, String> params);
}
