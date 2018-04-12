package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public abstract class MappableToDeviceFormatter {

    /**
     * Provides a {@link String} representation for the type of the given {@link MappableToDevice mappedTo} instance
     * depending on the actual sub-class type.
     * 
     * @param mappedTo
     *            {@link MappableToDevice} instance to get representation for
     * @return the {@link String} representing of the given mappedTo instance
     */
    public static String formatType(final MappableToDevice mappedTo, final StringMessages stringMessages) {
        if (mappedTo instanceof CompetitorWithBoatDTO || mappedTo instanceof CompetitorDTO) {
            return stringMessages.competitor();
        } else if (mappedTo instanceof BoatDTO) {
            return stringMessages.boat();
        } else if (mappedTo instanceof MarkDTO) {
            return stringMessages.mark();
        }
        return "";
    }

    /**
     * Provides a {@link String} representation for the given {@link MappableToDevice mappedTo} instance depending on
     * the actual sub-class type.
     * 
     * @param mappedTo
     *            {@link MappableToDevice} instance to get representation for
     * @return the {@link String} representing of the given mappedTo instance
     */
    public static String formatName(final MappableToDevice mappedTo) {
        if (mappedTo instanceof CompetitorWithBoatDTO || mappedTo instanceof CompetitorDTO) {
            return ((CompetitorDTO) mappedTo).getName();
        } else if (mappedTo instanceof BoatDTO) {
            return ((BoatDTO) mappedTo).getDisplayName();
        } else if (mappedTo instanceof MarkDTO) {
            return ((MarkDTO) mappedTo).getName();
        }
        return "";
    }
}
