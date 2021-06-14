package com.sap.sailing.hanaexport.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class HanaConnectionFactoryImpl {
    private static final Logger logger = Logger.getLogger(HanaConnectionFactoryImpl.class.getName());
    private static final String HANADB_ENDPOINT_PROPERTY_NAME = "sap.hana.endpoint"; 
    private static final String HANADB_USERNAME_SYSTEM_PROPERTY_NAME = "sap.hana.username";
    private static final String HANADB_PASSWORD_SYSTEM_PROPERTY_NAME = "sap.hana.password";
    
    public void test() throws SQLException {
        final Connection connection = DriverManager.getConnection(
                "jdbc:sap://"+System.getProperty(HANADB_ENDPOINT_PROPERTY_NAME)+"/?encrypt=true&validateCertificate=false",
                System.getProperty(HANADB_USERNAME_SYSTEM_PROPERTY_NAME),
                System.getProperty(HANADB_PASSWORD_SYSTEM_PROPERTY_NAME));
        if (connection == null) {
            logger.warning("Couldn't get database connection for end point "+System.getProperty(HANADB_ENDPOINT_PROPERTY_NAME));
        } else {
            final Statement stmt = connection.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT * FROM SAILING.BOAT_CLASS");
            while (resultSet.next()) {
                logger.info(resultSet.getString("id") + " " + resultSet.getString("description"));
            }
        }
    }
    
    public static void main(String[] args) throws SQLException {
        new HanaConnectionFactoryImpl().test();
    }
}
