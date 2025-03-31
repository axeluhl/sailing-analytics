package com.sap.sailing.landscape.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sse.common.Util;

public class TestReleaseRepository {
    @Test
    public void testForAtLeastOneRelease() {
        assertFalse(Util.isEmpty(SailingReleaseRepository.INSTANCE));
    }

    @Test
    public void testForAtLeastOneMasterRelease() {
        assertNotNull(SailingReleaseRepository.INSTANCE.getLatestMasterRelease());
    }
}
