package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sailing.gwt.ui.shared.TagDTOJSONDeSerializer;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TagDTOJSONDeSerializerTest {
    
    @Test
    public void testDeSerialization() {
        TagDTOJSONDeSerializer serializer = new TagDTOJSONDeSerializer();
        TagDTO tag1 = new TagDTO("1", "2", "3", "4", true, MillisecondsTimePoint.BeginningOfTime, MillisecondsTimePoint.now(), MillisecondsTimePoint.EndOfTime);
        TagDTO tag2 = new TagDTO("5", "6", "7", "8", true, MillisecondsTimePoint.now(), MillisecondsTimePoint.EndOfTime, MillisecondsTimePoint.BeginningOfTime);
        TagDTO tag3 = new TagDTO("5", "6", "7", "8", true, MillisecondsTimePoint.now(), MillisecondsTimePoint.EndOfTime, null);
        List<TagDTO> tags= new ArrayList<TagDTO>();
        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);
        List<TagDTO> deSerializedTags = serializer.deserialize(serializer.serialize(tags));
        assertEquals(tags.size(), deSerializedTags.size());
        for(int i = 0; i < tags.size(); i++) {
            assertEquals(tags.get(i), deSerializedTags.get(i));
        }
    }
}
