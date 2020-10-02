package com.sap.sse.landscape.aws;

import java.util.Map.Entry;

import com.sap.sse.landscape.aws.impl.TagsImpl;

public interface Tags extends Iterable<Entry<String, String>>{
    static Tags with(String key, String value) {
        return new TagsImpl(key, value);
    }
    
    Tags and(String key, String value);
}
