package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.sap.sailing.gwt.ui.client.GwtJsonDeSerializer;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.common.filter.ValueFilter;

/**
 * Serializes and deserializes {@link TagFilterSets}.
 */

public class TagFilterSetsJsonDeSerializer implements GwtJsonDeSerializer<TagFilterSets> {
    public static final String FIELD_ACTIVE_FILTERSET = "activeFilterSet";
    public static final String FIELD_FILTERSETS = "filterSets";
    public static final String FIELD_FILTERSET_NAME = "name";
    public static final String FIELD_FILTERSET_ISEDITABLE = "isEditable";
    public static final String FIELD_FILTERS = "filters";

    
    /**
     * Serializes {@link TagFilterSets tagFilterSets} to {@link JSONObject json}.
     * 
     * @param tagFilterSets
     *           {@link TagFilterSets} to be serialized
     * @return {@link JSONObject json object}
     */
    @Override
    public JSONObject serialize(TagFilterSets tagFilterSets) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ACTIVE_FILTERSET,
                tagFilterSets.getActiveFilterSet() != null ? new JSONString(tagFilterSets.getActiveFilterSet().getName())
                        : JSONNull.getInstance());
        JSONArray filterSetArray = new JSONArray();
        result.put(FIELD_FILTERSETS, filterSetArray);
        int i = 0;
        for (FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet : tagFilterSets.getFilterSets()) {
            // only editable filter sets are stored
            if (filterSet.isEditable()) {
                JSONObject filterSetObject = new JSONObject();
                filterSetArray.set(i++, filterSetObject);
                filterSetObject.put(FIELD_FILTERSET_NAME, new JSONString(filterSet.getName()));
                filterSetObject.put(FIELD_FILTERSET_ISEDITABLE, JSONBoolean.getInstance(filterSet.isEditable()));
                JSONArray filterArray = new JSONArray();
                filterSetObject.put(FIELD_FILTERS, filterArray);
                int j = 0;
                for (FilterWithUI<TagDTO> filter : filterSet.getFilters()) {
                    // Remark: other filter types than ValueFilter's are not stored right now
                    if (filter instanceof ValueFilter<?, ?>) {
                        ValueFilter<?, ?> valueFilter = (ValueFilter<?, ?>) filter;
                        JSONObject filterObject = ValueFilterJsonDeSerializerUtil.serialize(valueFilter);
                        filterArray.set(j++, filterObject);
                    }
                }
            }
        }
        return result;
    }

    
    /**
     * Deserializes {@link JSONObject json} to {@link TagFilterSets tagFilterSets}.
     * 
     * @param rootObject
     *           JSONObject to be deserialized
     * @return {@link TagFilterSets tagFilterSets}
     */
    @Override
    public TagFilterSets deserialize(JSONObject rootObject) {
        TagFilterSets result = null;
        if (rootObject != null) {
            result = new TagFilterSets();
            JSONValue activeFilterSetValue = rootObject.get(FIELD_ACTIVE_FILTERSET);
            String activeFilterSetName;
            if (activeFilterSetValue.isNull() != null) {
                activeFilterSetName = null;
            } else {
                activeFilterSetName = ((JSONString) activeFilterSetValue).stringValue();
            }
            JSONArray filterSetsArray = (JSONArray) rootObject.get(FIELD_FILTERSETS);
            for (int i = 0; i < filterSetsArray.size(); i++) {
                JSONObject filterSetValue = (JSONObject) filterSetsArray.get(i);
                JSONString filterSetNameValue = (JSONString) filterSetValue.get(FIELD_FILTERSET_NAME);
                JSONBoolean filterSetIsEditableValue = (JSONBoolean) filterSetValue.get(FIELD_FILTERSET_ISEDITABLE);
                FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet = new FilterSet<>(filterSetNameValue.stringValue());
                filterSet.setEditable(filterSetIsEditableValue.booleanValue());
                result.addFilterSet(filterSet);
                JSONArray filterArray = (JSONArray) filterSetValue.get(FIELD_FILTERS);
                for (int j = 0; j < filterArray.size(); j++) {
                    JSONObject filterObject = (JSONObject) filterArray.get(j);
                    JSONString filterType = (JSONString) filterObject
                            .get(ValueFilterJsonDeSerializerUtil.FIELD_FILTER_TYPE);
                    if (filterType != null
                            && ValueFilterJsonDeSerializerUtil.VALUE_FILTER_TYPE.equals(filterType.stringValue())) {
                        FilterWithUI<TagDTO> filterWithUI = TagValueFilterJsonDeSerializerUtil.deserialize(filterObject);
                        if (filterWithUI != null) {
                            filterSet.addFilter(filterWithUI);
                        }
                    }
                }
            }
            // finally set the active filter set
            if (activeFilterSetName != null) {
                for (FilterSet<TagDTO, FilterWithUI<TagDTO>> filterSet : result.getFilterSets()) {
                    if (activeFilterSetName.equals(filterSet.getName())) {
                        result.setActiveFilterSet(filterSet);
                        break;
                    }
                }
            }
        }
        return result;
    }
}
