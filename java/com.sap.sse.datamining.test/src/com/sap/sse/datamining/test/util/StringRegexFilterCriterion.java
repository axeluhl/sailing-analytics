package com.sap.sse.datamining.test.util;

import com.sap.sse.datamining.impl.criterias.RegexFilterCriterion;

public class StringRegexFilterCriterion extends RegexFilterCriterion<String> {

    public StringRegexFilterCriterion(String regex) {
        super(String.class, regex);
    }

    @Override
    protected String getValueToMatch(String data) {
        return data;
    }

}