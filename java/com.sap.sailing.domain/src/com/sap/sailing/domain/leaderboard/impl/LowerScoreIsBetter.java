package com.sap.sailing.domain.leaderboard.impl;

import java.util.Comparator;

/**
 * The score comparator as used by the ISAF standard scoring scheme. Lower scores are better.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LowerScoreIsBetter implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
        return o1-o2;
    }

}
