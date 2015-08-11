package com.sap.sailing.datamining.shared.dto;

public class DistanceDTO {

    private double geographicalMiles;
    private double seaMiles;
    private double nauticalMiles;
    private double meters;
    private double kilometers;
    private double centralAngleDegree;
    private double centralAngleRadian;

    /**
     * <b>Do NOT use!</b><br>
     * Constructor for GWT-Serialization.
     */
    @Deprecated
    DistanceDTO() {
    }

    public DistanceDTO(double geographicalMiles, double seaMiles, double nauticalMiles, double meters,
            double kilometers, double centralAngleDegree, double centralAngleRadian) {
        this.geographicalMiles = geographicalMiles;
        this.seaMiles = seaMiles;
        this.nauticalMiles = nauticalMiles;
        this.meters = meters;
        this.kilometers = kilometers;
        this.centralAngleDegree = centralAngleDegree;
        this.centralAngleRadian = centralAngleRadian;
    }

    public double getGeographicalMiles() {
        return geographicalMiles;
    }

    public double getSeaMiles() {
        return seaMiles;
    }

    public double getNauticalMiles() {
        return nauticalMiles;
    }

    public double getMeters() {
        return meters;
    }

    public double getKilometers() {
        return kilometers;
    }

    public double getCentralAngleDegree() {
        return centralAngleDegree;
    }

    public double getCentralAngleRadian() {
        return centralAngleRadian;
    }

}
