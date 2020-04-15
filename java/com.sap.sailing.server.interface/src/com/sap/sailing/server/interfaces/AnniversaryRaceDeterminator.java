package com.sap.sailing.server.interfaces;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sse.common.Util.Pair;

/**
 * This class implements the calculation and management of anniversary races. The base of the anniversary calculation is
 * a list of {@link SimpleRaceInfo races} consisting of known remote races taken from {@link RemoteSailingServerSet} and
 * the set of locally available races taken from {@link RacingEventService}. The anniversaries to be found are defined
 * by a set of {@link AnniversaryChecker} instances given to the constructor. On every
 * {@link AnniversaryRaceDeterminatorImpl#update() update} a list of all known races is created and ordered by the
 * startTimePoint. With this, anniversaries are defined by their startTimePoint at the moment when the list exceeds a
 * number that matches an anniversary to be found. Even if an older race is added afterwards, the initially determined
 * nth race will stay the same.
 */
public interface AnniversaryRaceDeterminator {
    /**
     * Interface for checker classes which are passed to the {@link AnniversaryRaceDeterminatorImpl}'s constructor in order to
     * determine anniversary numbers based on the {@link AnniversaryChecker#update(int) provided race count}.
     */
    public interface AnniversaryChecker {

        /**
         * Updates the internal state based and is required to be called on any race count change to avoid stale data.
         * <b>NOTE: </b>The getter methods for {@link #getAnniversaries() past} or {@link #getNextAnniversary() next}
         * anniversaries may only be called after the update is processed.
         * 
         * @param raceCount
         *            the total number of races
         */
        void update(int raceCount);

        /**
         * Given the {@link #update(int) current number of races}, this method should return a list containing all past
         * anniversary numbers.
         * 
         * @return a list of all past anniversary numbers
         */
        List<Integer> getAnniversaries();

        /**
         * Given the {@link #update(int) current number of races}, this method should provide the next anniversary.
         * 
         * @return the next anniversary number, or <code>null</code> if next anniversary cannot be determined
         */
        Integer getNextAnniversary();

        /**
         * Provides the {@link AnniversaryType type} of the {@link AnniversaryChecker}.
         * 
         * @return the {@link AnniversaryChecker}'s {@link AnniversaryType type}
         */
        AnniversaryType getType();
    }

    void setNextAnniversary(Pair<Integer, AnniversaryType> nextAnniversary);
    
    void setRaceCount(int raceCount);

    Pair<Integer, AnniversaryType> getNextAnniversary();

    Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> getKnownAnniversaries();
    
    void setKnownAnniversaries(Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> anniversaries);

    int getCurrentRaceCount();
    
    void start();
    
    void clearAndStop();

    void clear();

    void addAnniversary(int anniversaryToCheck, Pair<DetailedRaceInfo, AnniversaryType> anniversaryData);
}
