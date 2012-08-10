package com.sap.sailing.domain.leaderboard.impl;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The score comparator as used by the ISAF standard scoring scheme. Lower scores are better.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LowerScoreIsBetter implements Comparator<Double>, Serializable {
    private static final long serialVersionUID = -2767385186133743330L;

    @Override
    public int compare(Double o1, Double o2) {
        return Double.valueOf(o1).compareTo(Double.valueOf(o2));
    }

}
