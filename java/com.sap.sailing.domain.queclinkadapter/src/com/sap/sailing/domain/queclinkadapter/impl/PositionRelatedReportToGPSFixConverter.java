package com.sap.sailing.domain.queclinkadapter.impl;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.queclinkadapter.MessageWithDeviceOrigin;
import com.sap.sailing.domain.queclinkadapter.PositionRelatedReport;
import com.sap.sailing.domain.racelogtracking.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneImeiIdentifierImpl;

/**
 * Takes objects of type {@link PositionRelatedReport} and produces objects of type {@link GPSFix} or
 * {@link GPSFixMoving} from then, depending on whether or not they have {@link PositionRelatedReport#getCogAndSog() COG
 * and SOG} information available.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PositionRelatedReportToGPSFixConverter {
    public GPSFix createGPSFixFromPositionRelatedReport(PositionRelatedReport report) {
        final GPSFix result;
        if (report.getCogAndSog() != null) {
            result = new GPSFixMovingImpl(report.getPosition(), report.getValidityTime(), report.getCogAndSog(), /* optionalTrueHeading */ null);
        } else {
            result = new GPSFixImpl(report.getPosition(), report.getValidityTime());
        }
        return result;
    }
    
    public SmartphoneImeiIdentifier getDeviceIdentifier(MessageWithDeviceOrigin message) {
        return new SmartphoneImeiIdentifierImpl(message.getImei());
    }
}
