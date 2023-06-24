package com.sap.sailing.domain.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sse.common.Util;

/**
 * Offers a comparator that ranks boat class names and time stamps by the quality of a match of the boat class names to
 * a given boat class name, and the currentness of a timestamp (e.g., from a race result). The ordering is fuzzy in the
 * sense that other than the obvious perfect case-insensitive and whitespace-eliminated boat class name matches the boat
 * class names are compared to the {@link BoatClassMasterdata#getDisplayName() display name} and the enumeration literal
 * and the {@link BoatClassMasterdata#getAlternativeNames() alternative names}. A match is sorted to the top. Assuming
 * that display names, literals and alternative names have to be distinct, this will then also be the best result. If no
 * match is found this way, prefix matches are tried. Prefixes covering more than 50% of the correct name are then preferred
 * over timestamp ordering. Percentage-wise longer prefixes matched are considered better than percentage-wise shorter matches.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FuzzyBoatClassNameMatcher {
    /**
     * Sorts the {@code eventNameBoatClassNameCapturedWhen} list as described in the class comment
     * 
     * @param boatClass
     *            the boat class to match
     */
    public void sortOfficialResultsByRelevance(final BoatClassDTO boatClass, List<Util.Pair<String, Util.Pair<String, Date>>> eventNameBoatClassNameCapturedWhen) {
        Collections.sort(eventNameBoatClassNameCapturedWhen,
                new Comparator<Util.Pair<String, Util.Pair<String, Date>>>() {
                    @Override
                    public int compare(Util.Pair<String, Util.Pair<String, Date>> o1, Util.Pair<String, Util.Pair<String, Date>> o2) {
                        final int result;
                        final double quality1 = getQualityOfBoatClassMatch(boatClass, o1.getB().getA());
                        final double quality2 = getQualityOfBoatClassMatch(boatClass, o2.getB().getA());
                        if (Math.max(quality1, quality2) >= 0.5) {
                            result = -Double.compare(quality1, quality2);
                        } else {
                            // both don't seem to have a reasonably qualified boat class; compare by time stamp; newest first
                            result = o2.getB().getB().compareTo(o1.getB().getB());
                        }
                        return result;
                    }
                });
    }
    
    private double getQualityOfBoatClassMatch(BoatClassDTO boatClassToMatch, String boatClassNameCandidate) {
        final BoatClassMasterdata boatClassMasterdata = BoatClassMasterdata.resolveBoatClass(boatClassToMatch.getName());
        final Set<String> allNamesToMatchAgainst = new HashSet<>();
        if (boatClassMasterdata != null) {
            allNamesToMatchAgainst.add(boatClassMasterdata.getDisplayName());
            allNamesToMatchAgainst.addAll(Arrays.asList(boatClassMasterdata.getAlternativeNames()));
        } else {
            allNamesToMatchAgainst.add(boatClassToMatch.getName());
        }
        double longestCommonSubstringShareSoFar = 0;
        final String lowercaseBoatClassNameCandidate = boatClassNameCandidate.toLowerCase();
        for (final String boatClassNameToMatch : allNamesToMatchAgainst) {
            final String lowerClassBoatClassNameToMatch = boatClassNameToMatch.toLowerCase();
            longestCommonSubstringShareSoFar = Math.max(
                    ((double) getLengthOfLongestCommonSubstringBasedOnLettersAndNumbersOnly(
                            lowerClassBoatClassNameToMatch, lowercaseBoatClassNameCandidate)) / (double) lowerClassBoatClassNameToMatch.length(),
                    longestCommonSubstringShareSoFar);
        }
        return longestCommonSubstringShareSoFar;
    }
    
    private int getLengthOfLongestCommonSubstringBasedOnLettersAndNumbersOnly(String lowerBoatClassNameToMatch,
            String lowercaseBoatClassNameCandidate) {
        final String lowerBoatClassNameToMatchLettersAndNumbersOnly = lowerBoatClassNameToMatch.replaceAll("[^a-zA-Z0-9]*", "");
        final String lowercaseBoatClassNameCandidateLettersAndNumbersOnly = lowercaseBoatClassNameCandidate.replaceAll("[^a-zA-Z0-9]*", "");
        return Util.getLengthOfLongestCommonSubsequence(lowerBoatClassNameToMatchLettersAndNumbersOnly, lowercaseBoatClassNameCandidateLettersAndNumbersOnly);
    }
}
