package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.domain.base.BoatClass;

public class InsertBoatClassStatement extends AbstractPreparedInsertStatement<BoatClass> {
    protected InsertBoatClassStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"BoatClass\" (\"id\", \"description\", \"hullLengthInMeters\", \"hullBeamInMeters\", \"hullType\") VALUES (?, ?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(BoatClass boatClass) throws SQLException {
        if (boatClass.getName() == null) {
            getPreparedStatement().setString(1, "<null>");
        } else {
            getPreparedStatement().setString(1, boatClass.getName().substring(0, Math.min(boatClass.getName().length(), 255)));
        }
        getPreparedStatement().setString(2, "Type "+boatClass.getHullType().name()+", length "+
                boatClass.getHullLength().getMeters()+"m, beam "+boatClass.getHullBeam().getMeters()+"m");
        setDouble(3, boatClass.getHullLength().getMeters());
        setDouble(4, boatClass.getHullBeam().getMeters());
        getPreparedStatement().setString(5, boatClass.getHullType().name());
    }
}
