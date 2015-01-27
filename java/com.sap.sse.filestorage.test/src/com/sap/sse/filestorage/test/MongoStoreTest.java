package com.sap.sse.filestorage.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.filestorage.FileStorageServicePropertyStore;
import com.sap.sse.filestorage.impl.MongoFileStorageServicePropertyStoreImpl;
import com.sap.sse.mongodb.MongoDBConfiguration;

public class MongoStoreTest {
    private MongoDBConfiguration config = MongoDBConfiguration.getDefaultTestConfiguration();
    
    @Before
    public void setup() {
        config.getService().getDB().dropDatabase();
    }

    @After
    public void tearDown() {
        config.getService().getDB().dropDatabase();
    }
    
    @Test
    public void testStoreAndReadProperties() {
        FileStorageServicePropertyStore store = new MongoFileStorageServicePropertyStoreImpl(config.getService());
        
        store.writeProperty("s1", "p1", "1");
        store.writeProperty("s1", "p2", "2");
        store.writeProperty("s2", "p3", "3");
        
        Map<String, String> properties = store.readAllProperties("s1");
        assertThat("two properties read for service 1", properties.size(), equalTo(2));
        assertThat("property 1 is read correctly for service 1", properties.get("p1"), equalTo("1"));
        
        properties = store.readAllProperties("s2");
        assertThat("one property read for service 2", properties.size(), equalTo(1));
    }
}
