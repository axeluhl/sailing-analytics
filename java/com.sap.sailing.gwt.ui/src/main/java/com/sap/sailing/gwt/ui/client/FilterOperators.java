package com.sap.sailing.gwt.ui.client;

/**
 * An enumeration of filter operators 
 * @author Frank
 */
public enum FilterOperators {
    Equals, /** Returns only records with the specified value */
    NotEqualTo, /** Returns only records that do not include the specified value */
    LessThan, /** Returns only records that are less than the specified value */
    LessThanEquals, /** Returns only records that are less than or equal to the specified value */
    GreaterThan, /** Returns only records that are more than the specified value */
    GreaterThanEquals, /** Returns only records that are more than or equal to the specified value */
    Contains, /** Returns only records that contain the specified value */
    NotContains, /** Returns only records that do not contain the specified value */
    StartsWith, /** Returns only records that start with the specified value */
    EndsWith, /** Returns only records that end with the specified value */
}
