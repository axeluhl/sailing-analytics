package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.TransientCompetitorStoreImpl;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;

public class CompetitorStoreTest {
    @Test
    public void testAddingToTransientStore() {
        CompetitorStore transientStore = new TransientCompetitorStoreImpl();
        DynamicCompetitor template = AbstractLeaderboardTest.createCompetitor("Test Competitor");
        Competitor competitor = transientStore.getOrCreateCompetitor(template.getId(), template.getName(), template.getTeam(), template.getBoat());
        assertTrue(competitor != template);
        Competitor competitor2 = transientStore.getOrCreateCompetitor(template.getId(), template.getName(), template.getTeam(), template.getBoat());
        assertSame(competitor, competitor2);
        assertEquals(DomainFactory.INSTANCE.getOrCreateNationality("GER"), competitor.getTeam().getNationality());
        DynamicTeam differentTeam = AbstractLeaderboardTest.createCompetitor("Test Competitor").getTeam();
        differentTeam.setNationality(DomainFactory.INSTANCE.getOrCreateNationality("GHA")); // Ghana
        Competitor competitor3 = transientStore.getOrCreateCompetitor(template.getId(), template.getName(), differentTeam, template.getBoat());
        assertSame(competitor, competitor3); // use existing competitor despite the different team
        assertSame(competitor.getTeam(), competitor3.getTeam());
        
        // now mark the competitor as to update from defaults
        transientStore.allowCompetitorResetToDefaults(competitor);
        Competitor competitor4 = transientStore.getOrCreateCompetitor(template.getId(), template.getName(), differentTeam, template.getBoat());
        assertSame(competitor, competitor4); // expecting an in-place update
        assertEquals(differentTeam.getNationality(), competitor4.getTeam().getNationality());
    }
}
