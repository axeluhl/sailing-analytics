package com.sap.sailing.hanaexport.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.hanaexport.HanaConnectionFactory;

public class HanaConnectionFactoryImpl implements HanaConnectionFactory {
    private static final Logger logger = Logger.getLogger(HanaConnectionFactoryImpl.class.getName());
    private static final String HANADB_ENDPOINT_PROPERTY_NAME = "sap.hana.endpoint"; 
    private static final String HANADB_USERNAME_SYSTEM_PROPERTY_NAME = "sap.hana.username";
    private static final String HANADB_PASSWORD_SYSTEM_PROPERTY_NAME = "sap.hana.password";
    
    public void test() throws SQLException {
        System.out.println("Java version: " + com.sap.db.jdbc.Driver.getJavaVersion());
        System.out.println("Minimum supported Java version and SAP driver version number: " + com.sap.db.jdbc.Driver.getVersionInfo());
        final Connection connection = getConnection();
        if (connection == null) {
            logger.warning("Couldn't get database connection for end point "+System.getProperty(HANADB_ENDPOINT_PROPERTY_NAME));
        } else {
            connection.createStatement().execute("DELETE FROM SAILING.BOAT_CLASS");
            final PreparedStatement insertBoatClasses = connection.prepareStatement("INSERT INTO SAILING.BOAT_CLASS (\"id\", \"description\") VALUES (?, ?);");
            for (final BoatClassMasterdata boatClassMasterdata : BoatClassMasterdata.values()) {
                insertBoatClasses.setString(1, boatClassMasterdata.getDisplayName());
                insertBoatClasses.setString(2, "Type "+boatClassMasterdata.getHullType().name()+", length "+
                        boatClassMasterdata.getHullLength().getMeters()+"m, beam "+boatClassMasterdata.getHullBeam().getMeters()+"m");
                insertBoatClasses.execute();
            }
            final Statement stmt = connection.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT * FROM SAILING.BOAT_CLASS");
            logger.info("Fetch size: "+resultSet.getFetchSize());
            while (resultSet.next()) {
                logger.info(resultSet.getString("id") + " " + resultSet.getString("description"));
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:sap://"+System.getProperty(HANADB_ENDPOINT_PROPERTY_NAME)+"/?encrypt=true&validateCertificate=false",
                System.getProperty(HANADB_USERNAME_SYSTEM_PROPERTY_NAME),
                System.getProperty(HANADB_PASSWORD_SYSTEM_PROPERTY_NAME));
    }
    
    public static void main(String[] args) throws SQLException {
        new HanaConnectionFactoryImpl().test();
    }
}
