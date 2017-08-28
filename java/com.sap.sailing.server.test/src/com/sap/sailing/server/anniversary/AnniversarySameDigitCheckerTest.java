package com.sap.sailing.server.anniversary;

import org.junit.Test;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.anniversary.PeriodicRaceListAnniversaryDeterminator.AnniversaryChecker;
import com.sap.sailing.server.anniversary.checker.SameDigitChecker;

import junit.framework.Assert;

public class AnniversarySameDigitCheckerTest {

    @Test
    public void at9999() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(9999);
        Assert.assertTrue(checker.getAnniversaries().isEmpty());
        Assert.assertEquals(11111, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.REPEATED_DIGIT, checker.getType());
    }

    @Test
    public void at10000() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(10000);
        System.out.println(checker.getAnniversaries());
        Assert.assertEquals(11111, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.REPEATED_DIGIT, checker.getType());
    }

    @Test
    public void at11111() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(11111);
        System.out.println(checker.getAnniversaries());
        Assert.assertTrue(checker.getAnniversaries().contains(11111));
        Assert.assertEquals(22222, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.REPEATED_DIGIT, checker.getType());
    }

    //
    @Test
    public void after11112() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(11112);
        Assert.assertTrue(checker.getAnniversaries().contains(11111));
        Assert.assertEquals(22222, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.REPEATED_DIGIT, checker.getType());
    }

    @Test
    public void at88888() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(88888);
        Assert.assertTrue(checker.getAnniversaries().contains(11111));
        Assert.assertTrue(checker.getAnniversaries().contains(22222));
        Assert.assertTrue(checker.getAnniversaries().contains(33333));
        Assert.assertTrue(checker.getAnniversaries().contains(44444));
        Assert.assertTrue(checker.getAnniversaries().contains(55555));
        Assert.assertTrue(checker.getAnniversaries().contains(66666));
        Assert.assertTrue(checker.getAnniversaries().contains(77777));
        Assert.assertTrue(checker.getAnniversaries().contains(88888));
        Assert.assertEquals(111111, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.REPEATED_DIGIT, checker.getType());
    }

    @Test
    public void after22221() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(22221);
        Assert.assertTrue(checker.getAnniversaries().contains(11111));
        Assert.assertEquals(22222, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.REPEATED_DIGIT, checker.getType());
    }

    @Test
    public void after22222() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(22222);
        Assert.assertTrue(checker.getAnniversaries().contains(11111));
        Assert.assertTrue(checker.getAnniversaries().contains(22222));
        Assert.assertEquals(33333, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.REPEATED_DIGIT, checker.getType());
    }

    @Test
    public void afterMaxValidRaceCount() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(999999999);
        Assert.assertTrue(checker.getAnniversaries().contains(11111));
        Assert.assertTrue(checker.getAnniversaries().contains(22222));
        Assert.assertTrue(checker.getAnniversaries().contains(33333));
        Assert.assertTrue(checker.getAnniversaries().contains(44444));
        Assert.assertTrue(checker.getAnniversaries().contains(55555));
        Assert.assertTrue(checker.getAnniversaries().contains(66666));
        Assert.assertTrue(checker.getAnniversaries().contains(77777));
        Assert.assertTrue(checker.getAnniversaries().contains(88888));
        Assert.assertTrue(checker.getAnniversaries().contains(111111));
        Assert.assertTrue(checker.getAnniversaries().contains(222222));
        Assert.assertTrue(checker.getAnniversaries().contains(333333));
        Assert.assertTrue(checker.getAnniversaries().contains(444444));
        Assert.assertTrue(checker.getAnniversaries().contains(555555));
        Assert.assertTrue(checker.getAnniversaries().contains(666666));
        Assert.assertTrue(checker.getAnniversaries().contains(777777));
        Assert.assertTrue(checker.getAnniversaries().contains(888888));
        Assert.assertTrue(checker.getAnniversaries().contains(1111111));
        Assert.assertTrue(checker.getAnniversaries().contains(2222222));
        Assert.assertTrue(checker.getAnniversaries().contains(3333333));
        Assert.assertTrue(checker.getAnniversaries().contains(4444444));
        Assert.assertTrue(checker.getAnniversaries().contains(5555555));
        Assert.assertTrue(checker.getAnniversaries().contains(6666666));
        Assert.assertTrue(checker.getAnniversaries().contains(7777777));
        Assert.assertTrue(checker.getAnniversaries().contains(8888888));
        Assert.assertTrue(checker.getAnniversaries().contains(11111111));
        Assert.assertTrue(checker.getAnniversaries().contains(22222222));
        Assert.assertTrue(checker.getAnniversaries().contains(33333333));
        Assert.assertTrue(checker.getAnniversaries().contains(44444444));
        Assert.assertTrue(checker.getAnniversaries().contains(55555555));
        Assert.assertTrue(checker.getAnniversaries().contains(66666666));
        Assert.assertTrue(checker.getAnniversaries().contains(77777777));
        Assert.assertTrue(checker.getAnniversaries().contains(88888888));
        Assert.assertTrue(checker.getAnniversaries().contains(111111111));
        Assert.assertTrue(checker.getAnniversaries().contains(222222222));
        Assert.assertTrue(checker.getAnniversaries().contains(333333333));
        Assert.assertTrue(checker.getAnniversaries().contains(444444444));
        Assert.assertTrue(checker.getAnniversaries().contains(555555555));
        Assert.assertTrue(checker.getAnniversaries().contains(666666666));
        Assert.assertTrue(checker.getAnniversaries().contains(777777777));
        Assert.assertTrue(checker.getAnniversaries().contains(888888888));
        Assert.assertEquals(1111111111, checker.getNextAnniversary().intValue());
        Assert.assertEquals(AnniversaryType.REPEATED_DIGIT, checker.getType());
    }

    /**
     * Tests that no runaway loop can happen, if the anniversary to check overflows internally. It is expected that the
     * resulting String cannot be converted to a number anymore if it exceeds IntegerRange.
     */
    @Test(expected = NumberFormatException.class)
    public void ensureInCaseOfBugNoFreeze() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(Integer.MAX_VALUE);
    }

    @Test(expected = IllegalStateException.class)
    public void illegalRaceCountTest() {
        AnniversaryChecker checker = new SameDigitChecker();
        checker.update(-1);
    }
}
