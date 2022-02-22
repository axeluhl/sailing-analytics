package com.sap.sse.landscape.aws;

import java.util.Map.Entry;

import com.sap.sse.landscape.aws.impl.TagsImpl;

public interface Tags extends Iterable<Entry<String, String>>{
    static Tags with(String key, String value) {
        return new TagsImpl(key, value);
    }
    
    /**
     * Adds the {@code key} to the map and sets its value. If the key was already
     * part of these {@link Tags} then its value is overwritten by this call.
     */
    Tags and(String key, String value);
    
    /**
     * {@link #and(String, String) Adds} all tags from {@code tags} to this map of tags.
     * Keys from {@code tags} that already exist in this tags map are overwritten.
     */
    default Tags andAll(Tags tags) {
        for (final Entry<String, String> tag : tags) {
            and(tag.getKey(), tag.getValue());
        }
        return this;
    }

    static Tags empty() {
        return new TagsImpl();
    }
}
