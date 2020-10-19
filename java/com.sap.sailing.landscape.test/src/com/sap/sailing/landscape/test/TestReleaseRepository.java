package com.sap.sailing.landscape.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.sap.sailing.landscape.ReleaseRepository;
import com.sap.sse.common.Util;

public class TestReleaseRepository {
    @Test
    public void testForAtLeastOneRelease() {
        assertFalse(Util.isEmpty(ReleaseRepository.SAILING_RELEASE_REPOSITORY));
    }

    @Test
    public void testForAtLeastOneMasterRelease() {
        assertNotNull(ReleaseRepository.SAILING_RELEASE_REPOSITORY.getLatestMasterRelease());
    }
}
