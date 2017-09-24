package com.sap.sailing.server.anniversary.checker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.anniversary.AnniversaryRaceDeterminator.AnniversaryChecker;

/**
 * This checker adds all races that are the 10kth,25kth,50kth or 75kth or are a factor 10 of one of those.
 */
public class SameDigitChecker implements AnniversaryChecker {
    private final List<Integer> pastAnniversaries;
    private Integer nextAnniversary = null;

    public SameDigitChecker() {
        pastAnniversaries = new CopyOnWriteArrayList<>();
    }

    @Override
    public void update(int raceCount) {
        if (raceCount < 0) {
            throw new IllegalStateException("Negative Raceamount " + raceCount);
        }
        pastAnniversaries.clear();

        int amount = 5;
        while (true) {
            // we do not use the 9 digit, as it is too close to the 10*x from the Quarterchecker
            for (int digit = 1; digit < 9; digit++) {
                String digitAsString = String.valueOf(digit);
                String toTest = "";
                for (int magnitude = 0; magnitude < amount; magnitude++) {
                    toTest += digitAsString;
                }
                int candidate = Integer.parseInt(toTest);
                if (candidate <= raceCount) {
                    pastAnniversaries.add(candidate);
                } else {
                    nextAnniversary = candidate;
                    return;
                }
            }
            // check one digit more, till the checked number is > the amount of races
            amount++;
        }
    }

    @Override
    public List<Integer> getAnniversaries() {
        return pastAnniversaries;
    }

    @Override
    public Integer getNextAnniversary() {
        return nextAnniversary;
    }

    @Override
    public AnniversaryType getType() {
        return AnniversaryType.REPEATED_DIGIT;
    }

}
