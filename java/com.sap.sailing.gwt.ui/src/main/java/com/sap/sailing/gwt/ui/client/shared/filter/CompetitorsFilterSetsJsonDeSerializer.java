package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
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
        for(FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> filterSet: filterSets.getFilterSets()) {
            JSONObject filterSetObject = new JSONObject();
            filterSetArray.set(i++, filterSetObject);

            filterSetObject.put(FIELD_FILTERSET_NAME, new JSONString(filterSet.getName()));

            JSONArray filterArray = new JSONArray();
            filterSetObject.put(FIELD_FILTERS, filterArray);
            int j = 0;
            for(ValueFilterWithUI<CompetitorDTO, ?> filter: filterSet.getFilters()) {
                JSONObject filterObject = new JSONObject();
                filterArray.set(j++, filterObject);

                filterObject.put(FIELD_FILTER_NAME, new JSONString(filter.getName()));
                filterObject.put(FIELD_FILTER_OPERATOR, new JSONString(filter.getOperator().getName()));
                filterObject.put(FIELD_FILTER_VALUE, new JSONString(filter.getValue().toString()));
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
                
                FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> filterSet = new FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>(filterSetNameValue.stringValue());
                result.addFilterSet(filterSet);

                JSONArray filterArray = (JSONArray) filterSetValue.get(FIELD_FILTERS); 
                for(int j = 0; j < filterArray.size(); j++) {
                    JSONObject filterValue = (JSONObject) filterArray.get(j);
                    JSONString filterNameValue = (JSONString) filterValue.get(FIELD_FILTER_NAME);
                    JSONString filterOperatorValue = (JSONString) filterValue.get(FIELD_FILTER_OPERATOR);
                    JSONString filterValueValue = (JSONString) filterValue.get(FIELD_FILTER_VALUE);
                    
                    ValueFilterWithUI<CompetitorDTO, ?> filter = CompetitorsFilterFactory.getFilter(filterNameValue.stringValue(), 
                            filterOperatorValue.stringValue(), filterValueValue.stringValue());
                    if (filter != null) {
                        filterSet.addFilter(filter);
                    }
                }
            }
            // finally set the active filter set
            if(activeFilterSetName != null) {
                for(FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> filterSet: result.getFilterSets()) {
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
