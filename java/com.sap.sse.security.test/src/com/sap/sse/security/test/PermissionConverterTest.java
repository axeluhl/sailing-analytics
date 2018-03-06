package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;

import org.apache.shiro.authz.permission.WildcardPermission;
import org.junit.Test;

import com.sap.sse.common.Util;
import com.sap.sse.security.impl.PermissionConverter;

public class PermissionConverterTest {
    @Test
    public void testPermissionParts() {
        String[] parts = new PermissionConverter().getPermissionParts(new WildcardPermission("LEADERBOARD:EDIT:KW2017 Laser Int."));
        assertEquals("leaderboard", parts[0]);
        assertEquals("edit", parts[1]);
        assertEquals("kw2017 laser int.", parts[2]);
    }

    @Test
    public void testSimpleObjectId() {
        Iterable<String> parts = new PermissionConverter().getObjectIdsAsString(new WildcardPermission("LEADERBOARD:EDIT:KW2017 Laser Int."));
        assertEquals(1, Util.size(parts));
        assertEquals("leaderboard/kw2017 laser int.", parts.iterator().next());
    }

    @Test
    public void testTwoObjectIds() {
        Iterable<String> parts = new PermissionConverter().getObjectIdsAsString(new WildcardPermission("LEADERBOARD:EDIT:KW2017 Laser Int.,SWC 2017 Miami N/17"));
        assertEquals(2, Util.size(parts));
        assertEquals("leaderboard/kw2017 laser int.", Util.get(parts, 0));
        assertEquals("leaderboard/swc 2017 miami n\\/17", Util.get(parts, 1));
    }

    @Test
    public void testTwoObjectIdsOneWithLeadingBlank() {
        Iterable<String> parts = new PermissionConverter().getObjectIdsAsString(new WildcardPermission("LEADERBOARD:EDIT:KW2017 Laser Int., SWC 2017 Miami N/17"));
        assertEquals(2, Util.size(parts));
        assertEquals("leaderboard/kw2017 laser int.", Util.get(parts, 0));
        assertEquals("leaderboard/ swc 2017 miami n\\/17", Util.get(parts, 1));
    }
    
    @Test
    public void testGetWildcardPermission() {
        com.sap.sse.security.shared.WildcardPermission wp = new PermissionConverter().getWildcardPermission(new WildcardPermission("LEADERBOARD:EDIT:KW2017 Laser Int."));
        assertEquals("LEADERBOARD:EDIT:KW2017 Laser Int.".toLowerCase(), wp.toString());
    }
}
