package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.Comparator;
import java.util.Date;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sse.common.Util.Pair;

/**
 * Implements the {@link VectorField} interface by providing real wind data from a <code>TrackedRace</code> which has
 * been received in the form of one or more {@link WindInfoForRaceDTO} objects. The field uses time and space distances
 * to weigh the wind measurements provided, leading to a spatially-resolved wind field that can be visualized. Its
 * bounds are infinite as time/space-weighed averages can generally be computed anywhere.
 * <p>
 * 
 * The vectors produced by this field are sized such that their x/y values represent 1/60th of a longitude/latitude
 * degree per hour which resembles knots (nautical miles per hour).
 * <p>
 * 
 * The maximum wind speed assumed by this wind field is 40kts which is used to calculate line widths and particle
 * speeds.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class WindInfoForRaceVectorField implements VectorField {
    private static final double MAX_WIND_SPEED_IN_KNOTS = 40;
    private final Bounds infiniteBounds = new BoundsImpl(new DegreePosition(-90, -180), new DegreePosition(90, 180));
    private final Weigher<Pair<PositionDTO, Date>> weigher;
    private final Comparator<WindDTO> windByTimePointComparator = new Comparator<WindDTO>() {
        @Override
        public int compare(WindDTO o1, WindDTO o2) {
            return o1.measureTimepoint > o2.measureTimepoint ? 1 : o1.measureTimepoint == o2.measureTimepoint ? 0 : -1;
        }
    };
    
    public WindInfoForRaceVectorField(WindInfoForRaceDTO windInfoForRace) {
        weigher = new PositionDTOAndDateWeigher(/* half confidence after milliseconds */ 3000,
                                               /* halfConfidenceDistance */ new MeterDistance(1000),
                                               getAverageLatitudeDeg(windInfoForRace));
    }
    
    private double getAverageLatitudeDeg(WindInfoForRaceDTO windInfoForRace) {
        double latSum = 0;
        long count = 0;
        for (Entry<WindSource, WindTrackInfoDTO> windSourceAndTrack : windInfoForRace.windTrackInfoByWindSource.entrySet()) {
            for (WindDTO wind : windSourceAndTrack.getValue().windFixes) {
                if (wind.position != null) {
                    latSum += wind.position.latDeg;
                    count++;
                }
            }
        }
        return latSum / count;
    }

    @Override
    public boolean inBounds(Position p) {
        // all positions are always considered in bounds as we'll always try to interpolate/extrapolate
        return true;
    }

    @Override
    public Vector getVector(Position p, Date at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getMotionScale(int zoomLevel) {
        // This implementation is copied from SimulatorField, hoping it does something useful in combination with
        // the Swarm implementation.
        return 0.07 * Math.pow(1.6, Math.min(1.0, 6.0 - zoomLevel));
    }
    
    @Override
    public double getParticleWeight(Position p, Vector v) {
        return v == null ? 0 : (v.length() / MAX_WIND_SPEED_IN_KNOTS);
    }

    /**
     * The implementation uses 3 coordinate space units (usually mapping to pixels if no zoom or other transformation is
     * in place for the Context2d canvas) for {@link #MAX_WIND_SPEED_IN_KNOTS}, decreasing linearly to 0 for zero speeds.
     */
    @Override
    public double getLineWidth(double speed) {
        return 3.*speed/MAX_WIND_SPEED_IN_KNOTS;
    }

    @Override
    public Bounds getFieldCorners() {
        return infiniteBounds;
    }

    @Override
    public double getParticleFactor() {
        return 2.0;
    }

    /**
     * {@link #MAX_WIND_SPEED_IN_KNOTS maximum wind speed} is fully opaque; zero speed is fully transparent
     */
    @Override
    public String getColor(double speed) {
        return "rgba(255,255,255,"+Math.min(255, Math.round(255.*speed/MAX_WIND_SPEED_IN_KNOTS))+")";
    }

}
