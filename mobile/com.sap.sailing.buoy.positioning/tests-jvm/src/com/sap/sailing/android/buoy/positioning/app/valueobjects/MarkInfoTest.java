package com.sap.sailing.android.buoy.positioning.app.valueobjects;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MarkInfoTest {

    @Test
    public void testMarkInfoName() {
        MarkInfo markInfo = new MarkInfo();
        String name = "test";
        markInfo.setName(name);
        assertEquals(markInfo.getName(), name);
    }
}
