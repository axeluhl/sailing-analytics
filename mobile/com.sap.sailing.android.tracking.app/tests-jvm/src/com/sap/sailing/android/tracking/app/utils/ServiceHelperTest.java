package com.sap.sailing.android.tracking.app.utils;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ServiceHelperTest {

    @Test
    public void testServiceHelperConstructor() {
        ServiceHelper serviceHelper = ServiceHelper.getInstance();
        assertNotNull(serviceHelper);
    }
}
