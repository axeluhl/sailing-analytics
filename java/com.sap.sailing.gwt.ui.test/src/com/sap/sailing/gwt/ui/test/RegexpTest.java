package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class RegexpTest {
    RegExp regexp = RegExp.compile("^([A-Z][A-Z][A-Z])[^0-9]*([0-9]*)$");
    
    @Test
    public void testSailIDParsing() {
        String sailID = "GER 61";
        String result = null;
        MatchResult m = regexp.exec(sailID);
        if (regexp.test(sailID)) {
            String iocCode = m.getGroup(1);
            if (iocCode != null && iocCode.trim().length() > 0) {
                String number = m.getGroup(2);
                result = iocCode + number;
            }
        }
        assertEquals("GER61", result);
    }
}
