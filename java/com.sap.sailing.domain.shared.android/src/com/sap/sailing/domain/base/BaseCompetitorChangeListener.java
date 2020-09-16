package com.sap.sailing.domain.base;

import java.net.URI;

import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

/**
 * Default implementation of the {@link CompetitorChangeListener} interface with all methods doing nothing by default.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface BaseCompetitorChangeListener extends CompetitorChangeListener {
    @Override
    default void nationalityChanged(WithNationality what, Nationality oldNationality, Nationality newNationality) {
    }

    @Override
    default void colorChanged(Color oldColor, Color newColor) {
    }

    @Override
    default void nameChanged(String oldName, String newName) {
    }

    @Override
    default void shortNameChanged(String oldShortName, String newShortName) {
    }

    @Override
    default void emailChanged(String oldEmail, String newEmail) {
    }

    @Override
    default void searchTagChanged(String oldSearchTag, String newSearchTag) {
    }

    @Override
    default void flagImageChanged(URI oldFlagImageURI, URI newFlagImageURI) {
    }

    @Override
    default void timeOnTimeFactorChanged(Double oldTimeOnTimeFactor, Double newTimeOnTimeFactor) {
    }

    @Override
    default void timeOnDistanceAllowancePerNauticalMileChanged(Duration oldTimeOnDistanceAllowancePerNauticalMile,
            Duration newTimeOnDistanceAllowancePerNauticalMile) {
    }

}
