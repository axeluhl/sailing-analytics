package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.Storage;

public final class DataMiningQueryForSailorProfilesPersistor {
    private DataMiningQueryForSailorProfilesPersistor() {

    }

    /** remove data mining queries from local storage */
    public static void removeDMQueriesFromLocalStorage() {
        if (Storage.isLocalStorageSupported()) {
            Storage store = Storage.getLocalStorageIfSupported();
            store.setItem(SailingSettingsConstants.DATAMINING_QUERY, new JSONArray().toString());
        }
    }

    /** write data mining query to local storage if the local storage is available */
    public static void writeDMQueriesToLocalStorageIfPossible(SailorProfileStatisticDTO answer,
            final String identifier) {
        if (Storage.isLocalStorageSupported() && answer.getDataMiningQuery() != null) {
            Storage store = Storage.getLocalStorageIfSupported();
            String item = store.getItem(SailingSettingsConstants.DATAMINING_QUERY);

            JSONArray arr;
            if (item != null) {
                arr = JSONParser.parseStrict(item).isArray();
            } else {
                arr = new JSONArray();
            }

            JSONObject json = new JSONObject();
            json.put("payload", new JSONString(answer.getDataMiningQuery()));
            json.put("creation", new JSONString("" + MillisecondsTimePoint.now().asMillis()));
            json.put("uuid", new JSONString(identifier));

            arr.set(arr.size(), json);
            store.setItem(SailingSettingsConstants.DATAMINING_QUERY, arr.toString());
        }
    }

}
