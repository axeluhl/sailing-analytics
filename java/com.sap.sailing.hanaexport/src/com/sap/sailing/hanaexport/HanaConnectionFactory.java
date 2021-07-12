package com.sap.sailing.hanaexport;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.hanaexport.impl.HanaConnectionFactoryImpl;

public interface HanaConnectionFactory {
    static HanaConnectionFactory INSTANCE = new HanaConnectionFactoryImpl();
    
    Connection getConnection() throws SQLException;
}
