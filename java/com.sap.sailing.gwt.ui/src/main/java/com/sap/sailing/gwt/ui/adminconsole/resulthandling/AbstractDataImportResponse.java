package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONParser;

/**
 * Abstract super-class for data import response overlay type providing convenience methods.
 */
class AbstractDataImportResponse extends JavaScriptObject {

    private static final Logger logger = Logger.getLogger(AbstractDataImportResponse.class.getName());

    /**
     * {@link JSONParser#parseStrict(String) Strictly parses} the provided {@link String} representation into JSON and
     * {@link JavaScriptObject#cast() casts} it to the desired type.
     * 
     * @param json
     *            {@link String} representation to parse into JSON
     * @param typeString
     *            {@link String} name of the actually desired type
     * @return the parsed and casted object or <code>null</code> if any {@link Exception exception} occurs
     */
    protected static final <T extends AbstractDataImportResponse> T parse(String json, String typeString) {
        try {
            return JSONParser.parseStrict(json).isObject().getJavaScriptObject().cast();
        } catch (Exception e) {
            logger.severe(() -> "Failed to parse import response to type " + typeString);
            return null;
        }
    }

    protected AbstractDataImportResponse() {
    }

    /**
     * Maps an array containing element of any type into a {@link List list} of the same type by keeping their order.
     * 
     * @param array
     *            the array which should be mapped
     * @return the {@link List list} containing the array's elements or an empty list if the array is <code>null</code>
     *         or empty
     */
    protected final <T> List<T> asList(T[] array) {
        return (array == null || array.length == 0) ? Collections.emptyList() : Arrays.asList(array);
    }

    /**
     * Maps a {@link JsArray JavaScript array} containing any type of {@link JavaScriptObject JavaScript objects} into a
     * {@link List list} of the same type by keeping their order.
     * 
     * @param array
     *            the {@link JsArray JavaScript array} which should be mapped
     * @return the {@link List list} containing the array's elements or an empty list if the array is <code>null</code>
     *         or empty
     */
    protected final <T extends JavaScriptObject> List<T> asList(JsArray<T> array) {
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        } else {
            final List<T> list = new ArrayList<T>(array.length());
            for (int index = 0; index < array.length(); index++) {
                list.add(array.get(index));
            }
            return list;
        }
    }
    
}
