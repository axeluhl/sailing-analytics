package com.sap.sailing.selenium.api.event;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class MarkApi {

    private static final String URL_ADD_MARK_TO_REGATTA = "api/v1/mark/addMarkToRegatta";

    private static final String ATTRIBUTE_REGATTA_NAME = "regattaName";
    private static final String ATTRIBUTE_MARK_NAME = "markName";
    private static final String ATTRIBUTE_MARK_ID = "markId";

    public Mark addMarkToRegatta(final ApiContext ctx, final String regattaName, final String markName) {
        final JSONObject json = new JSONObject();
        json.put(ATTRIBUTE_REGATTA_NAME, regattaName);
        json.put(ATTRIBUTE_MARK_NAME, markName);
        return new Mark(ctx.post(URL_ADD_MARK_TO_REGATTA, null, json));
    }

    public class Mark extends JsonWrapper {

        private Mark(JSONObject json) {
            super(json);
        }

        public final UUID getMarkId() {
            return UUID.fromString(get(ATTRIBUTE_MARK_ID));
        }

    }

}
