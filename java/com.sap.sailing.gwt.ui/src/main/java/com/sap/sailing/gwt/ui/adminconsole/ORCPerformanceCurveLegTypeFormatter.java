package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ORCPerformanceCurveLegTypeFormatter {
    public static String getDescription(ORCPerformanceCurveLegTypes t, StringMessages stringMessages) {
        switch (t) {
        case CIRCULAR_RANDOM:
            return stringMessages.orcPerformanceCurveLegTypeDescriptionCircularRandom();
        case LONG_DISTANCE:
            return stringMessages.orcPerformanceCurveLegTypeDescriptionLongDistance();
        case NON_SPINNAKER:
            return stringMessages.orcPerformanceCurveLegTypeDescriptionNonSpinnaker();
        case TWA:
            return stringMessages.orcPerformanceCurveLegTypeDescriptionConstructedCourse();
        case WINDWARD_LEEWARD:
            return stringMessages.orcPerformanceCurveLegTypeDescriptionWindwardLeeward();
        case WINDWARD_LEEWARD_REAL_LIVE:
            return stringMessages.orcPerformanceCurveLegTypeDescriptionWindwardLeewardRealLive();
        }
        throw new RuntimeException("Internal error: don't know ORC performance curve leg type "+t.name());
    }

}
