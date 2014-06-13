package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.Date;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sse.common.Util.Pair;

/**
 * Implements the {@link VectorField} interface by providing real wind data from a <code>TrackedRace</code> which
 * has been received in the form of one or more {@link WindInfoForRaceDTO} objects. The field uses time and space
 * distances to weigh the wind measurements provided, leading to a spatially-resolved wind field that can be
 * visualized. Its bounds are infinite as time/space-weighed averages can generally be computed anywhere.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class WindInfoForRaceVectorField implements VectorField {
    private final Weigher<Pair<PositionDTO, Date>> weigher;
    
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Vector getVector(Position p, Date at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getMaxLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double motionScale(int zoomLevel) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double particleWeight(Position p, Vector v) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double lineWidth(double speed) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Bounds getFieldCorners() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getParticleFactor() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getColor(double speed) {
        // TODO Auto-generated method stub
        return null;
    }

}
