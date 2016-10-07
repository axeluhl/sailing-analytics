package com.sap.sailing.android.buoy.positioning.app.valueobjects;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MarkInfoTest {

    @Test
    public void testMarkInfoName() {
        String name = "test";
        MarkInfo markInfo = new MarkInfo(name);
        assertEquals(markInfo.getName(), name);
    }
}
