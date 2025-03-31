package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public abstract class AbstractPreparedInsertStatement<T> implements PreparedInsertStatement<T> {
    private final PreparedStatement preparedStatement;
    
    protected AbstractPreparedInsertStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }
    
    @Override
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
    
    protected void setDouble(int parameterIndex, double value) throws SQLException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            setDouble(parameterIndex, 0.0);
        } else {
            getPreparedStatement().setDouble(parameterIndex, value);
        }
    }
    
    protected int intOr0ForNull(Integer i) {
        return i==null?0:i;
    }

    protected double metersOr0ForNull(final Distance distance) {
        return distance == null ? 0 : distance.getMeters();
    }

    protected double secondsOr0ForNull(final Duration duration) {
        return duration == null ? 0 : duration.asSeconds();
    }
}
