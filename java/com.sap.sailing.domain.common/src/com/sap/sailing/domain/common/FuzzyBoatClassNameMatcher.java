package com.sap.sailing.domain.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

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
    private static final TreeMap<String, BoatClassMasterdata> boatClassMasterDataByLiteralAndDisplayNameAndAlternativeNames;
    
    static {
        boatClassMasterDataByLiteralAndDisplayNameAndAlternativeNames = new TreeMap<>();
        for (final BoatClassMasterdata bcm : BoatClassMasterdata.values()) {
            boatClassMasterDataByLiteralAndDisplayNameAndAlternativeNames.put(bcm.name().toLowerCase(), bcm);
            boatClassMasterDataByLiteralAndDisplayNameAndAlternativeNames.put(bcm.getDisplayName().toLowerCase(), bcm);
            for (final String alternativeName : bcm.getAlternativeNames()) {
                boatClassMasterDataByLiteralAndDisplayNameAndAlternativeNames.put(alternativeName.toLowerCase(), bcm);
            }
        }
    }
    
    /**
     * Sorts the {@code eventNameBoatClassNameCapturedWhen} list as described in the class comment
     * 
     * @param boatClassName
     *            the boat class name to match; it may still contain whitespace and may be capitalized in any way; for
     *            the purpose of comparing, whitespace and case will be ignored.
     */
    public void sortOfficialResultsByRelevance(final String boatClassName, List<Util.Pair<String, Util.Pair<String, Date>>> eventNameBoatClassNameCapturedWhen) {
        final String lowercaseBoatClassNameToMatch = boatClassName.toLowerCase();
        Collections.sort(eventNameBoatClassNameCapturedWhen,
                new Comparator<Util.Pair<String, Util.Pair<String, Date>>>() {
                    @Override
                    public int compare(Util.Pair<String, Util.Pair<String, Date>> o1, Util.Pair<String, Util.Pair<String, Date>> o2) {
                        int result;
                        if (isBoatClassMatch(lowercaseBoatClassNameToMatch, o1.getB().getA().toLowerCase())) {
                            if (isBoatClassMatch(lowercaseBoatClassNameToMatch, o2.getB().getA().toLowerCase())) {
                                // both don't seem to have the right boat class; compare by time stamp; newest first
                                result = o2.getB().getB().compareTo(o1.getB().getB());
                            } else {
                                result = -1; // o1 scores "better", comes first, because it has the right boat class name
                            }
                        } else if (o2.getB().getA() != null
                                && isBoatClassMatch(lowercaseBoatClassNameToMatch, o2.getB().getA().toLowerCase())) {
                            result = 1;
                        } else {
                            // both don't seem to have the right boat class; compare by time stamp; newest first
                            result = o2.getB().getB().compareTo(o1.getB().getB());
                        }
                        return result;
                    }
                });
    }
    
    private double getQualityOfBoatClassMatch(String lowercaseBoatClassNameCandidate) {
        final SortedMap<String, BoatClassMasterdata> tailMap = boatClassMasterDataByLiteralAndDisplayNameAndAlternativeNames.tailMap(lowercaseBoatClassNameCandidate);
        double bestResultSoFar = 0.0;
        for (final Entry<String, BoatClassMasterdata> e : tailMap.entrySet()) {
            if (!e.getKey().startsWith(lowercaseBoatClassNameCandidate)) {
                break;
            }
            
        }
        return 0.0;
    }
    
    private boolean isBoatClassMatch(String lowercaseBoatClassNameCandidate, String lowercaseBoatClassName) {
        // First try a quick match for the lowercase boat class name in the set:
        boolean result = lowercaseBoatClassNameCandidate.equals(lowercaseBoatClassName) ||
                // then try prefix match
                lowercaseBoatClassName.startsWith(lowercaseBoatClassNameCandidate);
        return result;
    }
}
