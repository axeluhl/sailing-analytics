package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTO;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public abstract class MappableToDeviceTypeFormatter {

    /**
     * Provides a {@link String} representation for the given {@link MappableToDevice mappedTo} instance depending on
     * the actual sub-class type.
     * 
     * @param mappedTo
     *            {@link MappableToDevice} instance to get representation for
     * @return the {@link String} representing of the given mappedTo instance
     */
    public static String format(final MappableToDevice mappedTo, final StringMessages stringMessages) {
        if (mappedTo instanceof CompetitorDTO || mappedTo instanceof CompetitorWithoutBoatDTO) {
            return stringMessages.competitor();
        } else if (mappedTo instanceof BoatDTO) {
            return stringMessages.boat();
        } else if (mappedTo instanceof MarkDTO) {
            return stringMessages.mark();
        }
        return stringMessages.unknown();
    }

}
