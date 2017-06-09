package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

/**
 * An equivalence class for regattas, based on their series and fleet structure as it can be
 * extracted from an XRR document. An instance can be constructed from a {@link RegattaDTO}. Two instances
 * are considerer equal ("in the same equivalence class") if their series names including their order
 * are equal and for all series the set of fleet names (regardless their order) is equal.<p>
 * 
 * The fleet order is ignored because the fleet ordering criteria cannot be extracted from the XRR
 * document, but equal fleet names are considered to imply equal ordering for those fleets that have
 * an ordering imposed (such as "Gold" and "Silver"). For fleets without implicit ordering, such as
 * for qualification fleets like "Yellow" and "Blue" ordering doesn't matter in the first place.
 * 
 * @author Axel Uhl (D043530)
 *
 */
class RegattaStructure {
    private final Iterable<String> seriesNamesInOrder;
    private final Map<String, LinkedHashSet<String>> seriesNamesToFleetNames;
    private final RankingMetrics rankingMetricType;
    
    public RegattaStructure(RegattaDTO regatta) {
        seriesNamesToFleetNames = new HashMap<>();
        List<String> mySeriesNamesInOrder = new ArrayList<>();
        seriesNamesInOrder = mySeriesNamesInOrder;
        for (SeriesDTO series : regatta.series) {
            mySeriesNamesInOrder.add(series.getName());
            LinkedHashSet<String> fleetNames = new LinkedHashSet<>();
            seriesNamesToFleetNames.put(series.getName(), fleetNames);
            for (FleetDTO fleet : series.getFleets()) {
                fleetNames.add(fleet.getName());
            }
        }
        this.rankingMetricType = regatta.rankingMetricType;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String seriesName : seriesNamesInOrder) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(seriesName);
            result.append(seriesNamesToFleetNames.get(seriesName));
        }
        return result.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rankingMetricType == null) ? 0 : rankingMetricType.hashCode());
        result = prime * result + ((seriesNamesInOrder == null) ? 0 : seriesNamesInOrder.hashCode());
        result = prime * result + ((seriesNamesToFleetNames == null) ? 0 : seriesNamesToFleetNames.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegattaStructure other = (RegattaStructure) obj;
        if (rankingMetricType != other.rankingMetricType)
            return false;
        if (seriesNamesInOrder == null) {
            if (other.seriesNamesInOrder != null)
                return false;
        } else if (!seriesNamesInOrder.equals(other.seriesNamesInOrder))
            return false;
        if (seriesNamesToFleetNames == null) {
            if (other.seriesNamesToFleetNames != null)
                return false;
        } else if (!seriesNamesToFleetNames.equals(other.seriesNamesToFleetNames))
            return false;
        return true;
    }
}