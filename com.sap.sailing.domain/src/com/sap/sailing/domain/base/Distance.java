package com.sap.sailing.domain.base;

public interface Distance {
    double getGeographicalMiles();

    double getSeaMiles();

    double getNauticalMiles();

    double getMeters();

    double getKilometers();

    double getCentralAngleDeg();

    double getCentralAngleRad();

    Distance scale(double factor);
}
