package com.sap.sailing.domain.tractracadapter;

import java.io.Serializable;
import java.util.Map;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP;
import com.sap.sailing.domain.tractracadapter.impl.RaceCourseReceiver;

/**
 * TracTrac objects can be augmented by what TracTrac calls a "DataSheet." These optional data sheets can provide
 * meta data about the object to which they belong. For example, a course's meta data can provide the passing
 * sides for each waypoint in the list. The race can provide a definition of side lines in its meta data. And a
 * control point can define its shape and color.<p>
 * 
 * This class encapsulates the patterns and procedures by which the additional data is extracted from the
 * respective TracTrac data. It can be used for both, clientparams.php or TTCM based data.
 * 
 * @see ClientParamsPHP
 * @see RaceCourseReceiver
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MetadataParser {
    public interface ControlPointMetaData extends Named {
        MarkType getType();
        String getColor();
        String getShape();
        String getPattern();
        Serializable getId();
    }
    
    Map<Integer, NauticalSide> parsePassingSideData(String routeMetadataString, Iterable<? extends TracTracControlPoint> controlPoints);

    /**
     * Returns as many metadata objects as there are marks in the control point (two for a gate, one otherwise)
     */
    Iterable<ControlPointMetaData> parseControlPointMetadata(TracTracControlPoint controlPoint);

    Map<String, Iterable<TracTracControlPoint>> parseSidelinesFromRaceMetadata(String raceMetadataString, Iterable<? extends TracTracControlPoint> controlPoints);

}
