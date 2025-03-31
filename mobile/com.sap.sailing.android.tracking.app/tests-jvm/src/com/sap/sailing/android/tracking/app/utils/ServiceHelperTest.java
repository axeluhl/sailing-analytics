package com.sap.sailing.android.tracking.app.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ServiceHelperTest {

    @Test
    public void testServiceHelperConstructor() {
        ServiceHelper serviceHelper = ServiceHelper.getInstance();
        assertNotNull(serviceHelper);
    }
}
