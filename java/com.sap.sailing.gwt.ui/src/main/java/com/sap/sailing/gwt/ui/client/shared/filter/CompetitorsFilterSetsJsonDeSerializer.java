package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.GwtJsonDeSerializer;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorsFilterSetsJsonDeSerializer implements GwtJsonDeSerializer<CompetitorsFilterSets> {
    public static final String FIELD_ACTIVE_FILTERSET = "activeFilterSet";
    public static final String FIELD_FILTERSETS = "filterSets";
    public static final String FIELD_FILTERSET_NAME = "name";
    public static final String FIELD_FILTERS = "filters";
    public static final String FIELD_FILTER_NAME = "name";
    public static final String FIELD_FILTER_OPERATOR = "operator";
    public static final String FIELD_FILTER_VALUE = "value";

    @Override
    public JSONObject serialize(CompetitorsFilterSets filterSets) {
        JSONObject result = new JSONObject();

        result.put(FIELD_ACTIVE_FILTERSET, filterSets.getActiveFilterSet() != null ? new JSONString(filterSets.getActiveFilterSet().getName()) : JSONNull.getInstance());

        JSONArray filterSetArray = new JSONArray();
        result.put(FIELD_FILTERSETS, filterSetArray);
        
        int i = 0;
        for(FilterSet<CompetitorDTO> filterSet: filterSets.getFilterSets()) {
            JSONObject filterSetObject = new JSONObject();
            filterSetArray.set(i++, filterSetObject);

            filterSetObject.put(FIELD_FILTERSET_NAME, new JSONString(filterSet.getName()));

            JSONArray filterArray = new JSONArray();
            filterSetObject.put(FIELD_FILTERS, filterArray);
            int j = 0;
            for(Filter<CompetitorDTO, ?> filter: filterSet.getFilters()) {
                JSONObject filterObject = new JSONObject();
                filterArray.set(j++, filterObject);

                filterObject.put(FIELD_FILTER_NAME, new JSONString(filter.getName()));
                filterObject.put(FIELD_FILTER_OPERATOR, new JSONString(filter.getConfiguration().getA().name()));
                filterObject.put(FIELD_FILTER_VALUE, new JSONString(filter.getConfiguration().getB().toString()));
            }
        }
        
        return result;
    }
    
    @Override
    public CompetitorsFilterSets deserialize(JSONValue filterSetsValue) {
        CompetitorsFilterSets result = null;
        
        if(filterSetsValue.isObject() != null) {
            result = new CompetitorsFilterSets();
            
            JSONObject rootObject = (JSONObject) filterSetsValue;
            JSONValue activeFilterSetValue = rootObject.get(FIELD_ACTIVE_FILTERSET);
            String activeFilterSetName;
            if(activeFilterSetValue.isNull() != null) {
                activeFilterSetName = null;
            } else {
                activeFilterSetName = ((JSONString) activeFilterSetValue).stringValue();
            }
            
            JSONArray filterSetsArray = (JSONArray) rootObject.get(FIELD_FILTERSETS);
            for(int i = 0; i < filterSetsArray.size(); i++) {
                JSONObject filterSetValue = (JSONObject) filterSetsArray.get(i);
                JSONString filterSetNameValue = (JSONString) filterSetValue.get(FIELD_FILTERSET_NAME);
                
                FilterSet<CompetitorDTO> filterSet = new FilterSet<CompetitorDTO>(filterSetNameValue.stringValue());
                result.addFilterSet(filterSet);

                JSONArray filterArray = (JSONArray) filterSetValue.get(FIELD_FILTERS); 
                for(int j = 0; j < filterArray.size(); j++) {
                    JSONObject filterValue = (JSONObject) filterArray.get(j);
                    JSONString filterNameValue = (JSONString) filterValue.get(FIELD_FILTER_NAME);
                    JSONString filterOperatorValue = (JSONString) filterValue.get(FIELD_FILTER_OPERATOR);
                    JSONString filterValueValue = (JSONString) filterValue.get(FIELD_FILTER_VALUE);
                    
                    Filter<CompetitorDTO, ?> filter = CompetitorsFilterFactory.getFilter(filterNameValue.stringValue());
                    FilterOperators op = FilterOperators.valueOf(filterOperatorValue.stringValue());
                    if (filter != null) {
                        if (filter.getValueType().equals(Integer.class)) {
                            @SuppressWarnings("unchecked") // TODO see bug 1356
                            Filter<CompetitorDTO, Integer> intFilter = (Filter<CompetitorDTO, Integer>) filter;
                            intFilter.setConfiguration(new Pair<FilterOperators, Integer>(op, Integer
                                    .parseInt(filterValueValue.stringValue())));
                            filterSet.addFilter(intFilter);
                        }
                        if (filter.getValueType().equals(String.class)) {
                            @SuppressWarnings("unchecked") // TODO see bug 1356
                            Filter<CompetitorDTO, String> stringFilter = (Filter<CompetitorDTO, String>) filter;
                            stringFilter.setConfiguration(new Pair<FilterOperators, String>(op, filterValueValue
                                    .stringValue()));
                            filterSet.addFilter(stringFilter);
                        }
                    }
                }
            }
            // finally set the active filter set
            if(activeFilterSetName != null) {
                for(FilterSet<CompetitorDTO> filterSet: result.getFilterSets()) {
                    if(activeFilterSetName.equals(filterSet.getName())) {
                        result.setActiveFilterSet(filterSet);
                        break;
                    }
                }
            }
        }
        
        return result;
    }
}
