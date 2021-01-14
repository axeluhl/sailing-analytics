package com.sap.sse.landscape.aws.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.landscape.aws.Tags;

public class TagsImpl implements Tags {
    private final Map<String, String> tags;
    
    public TagsImpl() {
        tags = new HashMap<>();
    }
    
    public TagsImpl(String key, String value) {
        this();
        and(key, value);
    }
    
    @Override
    public Tags and(String key, String value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return tags.entrySet().iterator();
    }
}
