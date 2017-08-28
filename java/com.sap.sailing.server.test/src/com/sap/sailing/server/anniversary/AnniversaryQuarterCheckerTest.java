package com.sap.sailing.server.anniversary;

import org.junit.Test;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.anniversary.PeriodicRaceListAnniversaryDeterminator.AnniversaryChecker;
import com.sap.sailing.server.anniversary.checker.QuarterChecker;

import junit.framework.Assert;

public class AnniversaryQuarterCheckerTest {

    @Test
    public void at9999() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(9999);
        Assert.assertTrue(checker.getAnniversaries().isEmpty());
        Assert.assertEquals(10000, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.QUARTER, checker.getType());
    }

    @Test
    public void at10000() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(10000);
        System.out.println(checker.getAnniversaries());
        Assert.assertTrue(checker.getAnniversaries().contains(10000));
        Assert.assertEquals(25000, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.QUARTER, checker.getType());
    }

    @Test
    public void after10001() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(10001);
        Assert.assertTrue(checker.getAnniversaries().contains(10000));
        Assert.assertEquals(25000, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.QUARTER, checker.getType());
    }

    @Test
    public void after11111() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(10001);
        Assert.assertTrue(checker.getAnniversaries().contains(10000));
        Assert.assertEquals(25000, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.QUARTER, checker.getType());
    }

    @Test
    public void after24999() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(24999);
        Assert.assertTrue(checker.getAnniversaries().contains(10000));
        Assert.assertEquals(25000, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.QUARTER, checker.getType());
    }

    @Test
    public void after25000() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(25000);
        Assert.assertTrue(checker.getAnniversaries().contains(10000));
        Assert.assertTrue(checker.getAnniversaries().contains(25000));
        Assert.assertEquals(50000, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.QUARTER, checker.getType());
    }

    @Test
    public void after25001() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(25001);
        Assert.assertTrue(checker.getAnniversaries().contains(10000));
        Assert.assertTrue(checker.getAnniversaries().contains(25000));
        Assert.assertEquals(50000, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.QUARTER, checker.getType());
    }

    @Test
    public void afterMaxValidRaceCount() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(999999999);
        Assert.assertTrue(checker.getAnniversaries().contains(10000));
        Assert.assertTrue(checker.getAnniversaries().contains(25000));
        Assert.assertTrue(checker.getAnniversaries().contains(50000));
        Assert.assertTrue(checker.getAnniversaries().contains(75000));
        Assert.assertTrue(checker.getAnniversaries().contains(100000));
        Assert.assertTrue(checker.getAnniversaries().contains(250000));
        Assert.assertTrue(checker.getAnniversaries().contains(500000));
        Assert.assertTrue(checker.getAnniversaries().contains(750000));
        Assert.assertTrue(checker.getAnniversaries().contains(1000000));
        Assert.assertTrue(checker.getAnniversaries().contains(2500000));
        Assert.assertTrue(checker.getAnniversaries().contains(5000000));
        Assert.assertTrue(checker.getAnniversaries().contains(7500000));
        Assert.assertTrue(checker.getAnniversaries().contains(10000000));
        Assert.assertTrue(checker.getAnniversaries().contains(25000000));
        Assert.assertTrue(checker.getAnniversaries().contains(50000000));
        Assert.assertTrue(checker.getAnniversaries().contains(75000000));
        Assert.assertTrue(checker.getAnniversaries().contains(100000000));
        Assert.assertTrue(checker.getAnniversaries().contains(250000000));
        Assert.assertTrue(checker.getAnniversaries().contains(500000000));
        Assert.assertTrue(checker.getAnniversaries().contains(750000000));
        Assert.assertEquals(1000000000, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.QUARTER, checker.getType());
    }

    /**
     * Tests that no runaway loop can happen, if the anniversary to check overflows internally. The used 1000000000 is
     * the first number, that will lead to a overflow, after *10 multiplier is applied, testing for the next anniversary
     */
    @Test(expected = IllegalStateException.class)
    public void ensureInCaseOfBugNoFreeze() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(1000000000);
    }

    @Test(expected = IllegalStateException.class)
    public void illegalRaceCountTest() {
        AnniversaryChecker checker = new QuarterChecker();
        checker.update(-1);
    }
}
