package com.sap.sailing.datamining.shared;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSettingsImpl extends ManeuverSettings {

    private static final long serialVersionUID = 69258712144L;

    private Double minManeuverDuration;
    private Double maxManeuverDuration;
    private Double minManeuverEnteringSpeedInKnots;
    private Double maxManeuverEnteringSpeedInKnots;
    private Double minManeuverExitingSpeedInKnots;
    private Double maxManeuverExitingSpeedInKnots;
    private Double minManeuverEnteringAbsTWA;
    private Double maxManeuverEnteringAbsTWA;
    private Double minManeuverExitingAbsTWA;
    private Double maxManeuverExitingAbsTWA;
    private boolean mainCurveAnalysis;

    public static ManeuverSettingsImpl createDefault() {
        return new ManeuverSettingsImpl(1.0, null, 2.0, null, 1.0, null, null, null, null, null, false);
    }

    public ManeuverSettingsImpl() {
    }

    public ManeuverSettingsImpl(Double minManeuverDuration, Double maxManeuverDuration,
            Double minManeuverEnteringSpeedInKnots, Double maxManeuverEnteringSpeedInKnots,
            Double minManeuverExitingSpeedInKnots, Double maxManeuverExitingSpeedInKnots,
            Double minManeuverEnteringAbsTWA, Double maxManeuverEnteringAbsTWA, Double minManeuverExitingAbsTWA,
            Double maxManeuverExitingAbsTWA, boolean mainCurveAnalysis) {
        super();
        this.minManeuverDuration = minManeuverDuration;
        this.maxManeuverDuration = maxManeuverDuration;
        this.minManeuverEnteringSpeedInKnots = minManeuverEnteringSpeedInKnots;
        this.maxManeuverEnteringSpeedInKnots = maxManeuverEnteringSpeedInKnots;
        this.minManeuverExitingSpeedInKnots = minManeuverExitingSpeedInKnots;
        this.maxManeuverExitingSpeedInKnots = maxManeuverExitingSpeedInKnots;
        this.minManeuverEnteringAbsTWA = minManeuverEnteringAbsTWA;
        this.maxManeuverEnteringAbsTWA = maxManeuverEnteringAbsTWA;
        this.minManeuverExitingAbsTWA = minManeuverExitingAbsTWA;
        this.maxManeuverExitingAbsTWA = maxManeuverExitingAbsTWA;
        this.mainCurveAnalysis = mainCurveAnalysis;
    }

    public Double getMinManeuverDuration() {
        return minManeuverDuration;
    }

    public Double getMaxManeuverDuration() {
        return maxManeuverDuration;
    }

    public Double getMinManeuverEnteringSpeedInKnots() {
        return minManeuverEnteringSpeedInKnots;
    }

    public Double getMaxManeuverEnteringSpeedInKnots() {
        return maxManeuverEnteringSpeedInKnots;
    }

    public Double getMinManeuverExitingSpeedInKnots() {
        return minManeuverExitingSpeedInKnots;
    }

    public Double getMaxManeuverExitingSpeedInKnots() {
        return maxManeuverExitingSpeedInKnots;
    }

    public Double getMinManeuverEnteringAbsTWA() {
        return minManeuverEnteringAbsTWA;
    }

    public Double getMaxManeuverEnteringAbsTWA() {
        return maxManeuverEnteringAbsTWA;
    }

    public Double getMinManeuverExitingAbsTWA() {
        return minManeuverExitingAbsTWA;
    }

    public Double getMaxManeuverExitingAbsTWA() {
        return maxManeuverExitingAbsTWA;
    }
    
    @Override
    public boolean isMainCurveAnalysis() {
        return mainCurveAnalysis;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mainCurveAnalysis ? 1231 : 1237);
        result = prime * result + ((maxManeuverDuration == null) ? 0 : maxManeuverDuration.hashCode());
        result = prime * result + ((maxManeuverEnteringAbsTWA == null) ? 0 : maxManeuverEnteringAbsTWA.hashCode());
        result = prime * result
                + ((maxManeuverEnteringSpeedInKnots == null) ? 0 : maxManeuverEnteringSpeedInKnots.hashCode());
        result = prime * result + ((maxManeuverExitingAbsTWA == null) ? 0 : maxManeuverExitingAbsTWA.hashCode());
        result = prime * result
                + ((maxManeuverExitingSpeedInKnots == null) ? 0 : maxManeuverExitingSpeedInKnots.hashCode());
        result = prime * result + ((minManeuverDuration == null) ? 0 : minManeuverDuration.hashCode());
        result = prime * result + ((minManeuverEnteringAbsTWA == null) ? 0 : minManeuverEnteringAbsTWA.hashCode());
        result = prime * result
                + ((minManeuverEnteringSpeedInKnots == null) ? 0 : minManeuverEnteringSpeedInKnots.hashCode());
        result = prime * result + ((minManeuverExitingAbsTWA == null) ? 0 : minManeuverExitingAbsTWA.hashCode());
        result = prime * result
                + ((minManeuverExitingSpeedInKnots == null) ? 0 : minManeuverExitingSpeedInKnots.hashCode());
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
        ManeuverSettingsImpl other = (ManeuverSettingsImpl) obj;
        if (mainCurveAnalysis != other.mainCurveAnalysis)
            return false;
        if (maxManeuverDuration == null) {
            if (other.maxManeuverDuration != null)
                return false;
        } else if (!maxManeuverDuration.equals(other.maxManeuverDuration))
            return false;
        if (maxManeuverEnteringAbsTWA == null) {
            if (other.maxManeuverEnteringAbsTWA != null)
                return false;
        } else if (!maxManeuverEnteringAbsTWA.equals(other.maxManeuverEnteringAbsTWA))
            return false;
        if (maxManeuverEnteringSpeedInKnots == null) {
            if (other.maxManeuverEnteringSpeedInKnots != null)
                return false;
        } else if (!maxManeuverEnteringSpeedInKnots.equals(other.maxManeuverEnteringSpeedInKnots))
            return false;
        if (maxManeuverExitingAbsTWA == null) {
            if (other.maxManeuverExitingAbsTWA != null)
                return false;
        } else if (!maxManeuverExitingAbsTWA.equals(other.maxManeuverExitingAbsTWA))
            return false;
        if (maxManeuverExitingSpeedInKnots == null) {
            if (other.maxManeuverExitingSpeedInKnots != null)
                return false;
        } else if (!maxManeuverExitingSpeedInKnots.equals(other.maxManeuverExitingSpeedInKnots))
            return false;
        if (minManeuverDuration == null) {
            if (other.minManeuverDuration != null)
                return false;
        } else if (!minManeuverDuration.equals(other.minManeuverDuration))
            return false;
        if (minManeuverEnteringAbsTWA == null) {
            if (other.minManeuverEnteringAbsTWA != null)
                return false;
        } else if (!minManeuverEnteringAbsTWA.equals(other.minManeuverEnteringAbsTWA))
            return false;
        if (minManeuverEnteringSpeedInKnots == null) {
            if (other.minManeuverEnteringSpeedInKnots != null)
                return false;
        } else if (!minManeuverEnteringSpeedInKnots.equals(other.minManeuverEnteringSpeedInKnots))
            return false;
        if (minManeuverExitingAbsTWA == null) {
            if (other.minManeuverExitingAbsTWA != null)
                return false;
        } else if (!minManeuverExitingAbsTWA.equals(other.minManeuverExitingAbsTWA))
            return false;
        if (minManeuverExitingSpeedInKnots == null) {
            if (other.minManeuverExitingSpeedInKnots != null)
                return false;
        } else if (!minManeuverExitingSpeedInKnots.equals(other.minManeuverExitingSpeedInKnots))
            return false;
        return true;
    }

}
