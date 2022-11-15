package com.sap.sailing.selenium.api.coursetemplate;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class RepeatablePart extends JsonWrapper {

    private static final String FIELD_ZERO_BASED_INDEX_OF_REPEATABLE_PART_START = "zeroBasedIndexOfRepeatablePartStart";
    private static final String FIELD_ZERO_BASED_INDEX_OF_REPEATABLE_PART_END = "zeroBasedIndexOfRepeatablePartEnd";

    public RepeatablePart(JSONObject json) {
        super(json);
    }

    public Integer getZeroBasedIndexOfRepeatablePartStart() {
        return ((Long) get(FIELD_ZERO_BASED_INDEX_OF_REPEATABLE_PART_START)).intValue();
    }

    public Integer getZeroBasedIndexOfRepeatablePartEnd() {
        return ((Long) get(FIELD_ZERO_BASED_INDEX_OF_REPEATABLE_PART_END)).intValue();
    }
}
