package com.sap.sse.datamining.test.util;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.datamining.SailingPredefinedQueries;
import com.sap.sse.datamining.shared.DataMiningQuerySerializer;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public class DataMiningQuerySerializerTest {

    @Test
    public void testSerializationAndDeserialization() {

        // test null
        testSerializationAndDeserialization(null);

        // test all predefined queries
        for (StatisticQueryDefinitionDTO query : new SailingPredefinedQueries().getQueries().values()) {
            testSerializationAndDeserialization(query);
        }
    }

    private void testSerializationAndDeserialization(final StatisticQueryDefinitionDTO dto) {
        String base64 = DataMiningQuerySerializer.toBase64String(dto);
        StatisticQueryDefinitionDTO deserialized = DataMiningQuerySerializer.fromBase64String(base64);
        Assert.assertEquals(dto, deserialized);
    }
}
