package com.sap.sailing.datamining.test.util;

import com.sap.sailing.datamining.impl.criterias.RegexFilterCriteria;

public class StringRegexFilterCriteria extends RegexFilterCriteria<String> {

    public StringRegexFilterCriteria(String regex) {
        super(regex);
    }

    @Override
    protected String getValueToMatch(String data) {
        return data;
    }

}