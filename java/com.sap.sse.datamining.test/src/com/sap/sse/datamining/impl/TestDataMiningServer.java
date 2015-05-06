package com.sap.sse.datamining.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;

import com.sap.sse.datamining.DataSourceProvider;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestDataMiningServer {

    @Test
    public void testDataSourceProviderManagement() {
        ModifiableDataMiningServer server = TestsUtil.createNewServer();
        DataSourceProvider<Test_Regatta> dataSourceProvider = new AbstractDataSourceProvider<Test_Regatta>(Test_Regatta.class) {
            @Override
            public Test_Regatta getDataSource() {
                return null;
            }
        };
        
        Date beforeChange = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.setDataSourceProvider(dataSourceProvider);
        assertThat(server.getComponentsChangedTimepoint().after(beforeChange), is(true));
        
        beforeChange = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.setDataSourceProvider(dataSourceProvider);
        assertThat(server.getComponentsChangedTimepoint().after(beforeChange), is(true));
        
        beforeChange = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.removeDataSourceProvider(dataSourceProvider);
        assertThat(server.getComponentsChangedTimepoint().after(beforeChange), is(true));
        
        beforeChange = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.removeDataSourceProvider(dataSourceProvider);
        assertThat(server.getComponentsChangedTimepoint().after(beforeChange), is(false));
    }

}
