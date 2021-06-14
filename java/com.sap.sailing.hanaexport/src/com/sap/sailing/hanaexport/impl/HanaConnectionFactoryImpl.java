package com.sap.sailing.hanaexport.impl;

import java.sql.DriverManager;
import java.sql.SQLException;

public class HanaConnectionFactoryImpl {
    private static final String HANADB_ENDPOINT_PROPERTY_NAME = "sap.hana.endpoint"; 
    private static final String HANADB_USERNAME_SYSTEM_PROPERTY_NAME = "sap.hana.username";
    private static final String HANADB_PASSWORD_SYSTEM_PROPERTY_NAME = "sap.hana.password";
    
    public void test() throws SQLException {
        DriverManager.getConnection(
                "jdbc:sap://"+System.getProperty(HANADB_ENDPOINT_PROPERTY_NAME)+"/?encrypt=true",
                System.getProperty(HANADB_USERNAME_SYSTEM_PROPERTY_NAME),
                System.getProperty(HANADB_PASSWORD_SYSTEM_PROPERTY_NAME));
        com.sap.db.jdbcext.XidSAP test = null; // make sure this compiles, meaning we have com.sap.db.jdbc on the classpath
        if (test == null) {
            System.out.println("All as expected");
        }
    }
}
