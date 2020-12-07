package com.sap.sailing.hanaexport.impl;

import java.sql.DriverManager;
import java.sql.SQLException;

public class HanaConnectionFactoryImpl {
    public void test() throws SQLException {
        DriverManager.getConnection(
                "jdbc:sap://myhdb:30715/?autocommit=false", "myName",
                   "mySecret");
        com.sap.db.jdbcext.XidSAP test = null; // make sure this compiles, meaning we have com.sap.db.jdbc on the classpath
        if (test == null) {
            System.out.println("All as expected");
        }
    }
}
