package com.sap.sse.common.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sse.common.Util;

public class KeywordQuery implements Query, Serializable {
    private static final long serialVersionUID = -4214703751947494064L;
    
    private Iterable<String> keywords;

    KeywordQuery() {} // for GWT RPC serialization
    
    public KeywordQuery(String... keywords) {
        this.keywords = trim(Arrays.asList(keywords));
    }
    
    private Iterable<String> trim(Iterable<String> keywords) {
        final List<String> result = new ArrayList<String>(Util.size(keywords));
        for (String keyword : keywords) {
            result.add(keyword.trim());
        }
        return result;
    }

    public KeywordQuery(Iterable<String> keywords) {
        super();
        this.keywords = trim(keywords);
    }

    public Iterable<String> getKeywords() {
        return keywords;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String keyword : keywords) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append('"');
            sb.append(keyword);
            sb.append('"');
        }
        return sb.toString();
    }
}
