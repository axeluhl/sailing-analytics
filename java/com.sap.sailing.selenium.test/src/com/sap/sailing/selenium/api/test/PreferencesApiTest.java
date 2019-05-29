package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.PreferencesApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class PreferencesApiTest extends AbstractSeleniumTest {

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void createAndGetPreferencesTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), ApiContext.SERVER_CONTEXT);

        Map<String, String> prefObjectAttr = new HashMap<String, String>();
        prefObjectAttr.put("key1", "value1");
        prefObjectAttr.put("key2", "value2");

        PreferencesApi preferencesApi = new PreferencesApi();
        preferencesApi.createPreference(ctx, "pref1", prefObjectAttr);

        JSONObject foundPreference = preferencesApi.getPreference(ctx, "pref1");
        assertEquals("restored preference is different", "value1", foundPreference.get("key1"));
        assertEquals("restored preference is different", "value2", foundPreference.get("key2"));
    }

    @Test
    public void createAndDeletePreferencesTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), ApiContext.SERVER_CONTEXT);

        Map<String, String> prefObjectAttr = new HashMap<String, String>();
        prefObjectAttr.put("key1", "value1");
        prefObjectAttr.put("key2", "value2");

        PreferencesApi preferencesApi = new PreferencesApi();
        preferencesApi.createPreference(ctx, "pref2", prefObjectAttr);
        preferencesApi.deletePreference(ctx, "pref2");

        JSONObject foundPreference = preferencesApi.getPreference(ctx, "pref2");
        assertNull("should not find a deleted preference", foundPreference);
    }

}
