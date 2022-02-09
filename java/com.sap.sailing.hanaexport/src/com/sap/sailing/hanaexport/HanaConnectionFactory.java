package com.sap.sailing.hanaexport;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import com.sap.sailing.hanaexport.impl.HanaConnectionFactoryImpl;

public interface HanaConnectionFactory {
    static HanaConnectionFactory INSTANCE = new HanaConnectionFactoryImpl();
    
    Connection getConnection(Optional<String> dbEndpoint, Optional<String> dbUser, Optional<String> dbPassword) throws SQLException;
}
