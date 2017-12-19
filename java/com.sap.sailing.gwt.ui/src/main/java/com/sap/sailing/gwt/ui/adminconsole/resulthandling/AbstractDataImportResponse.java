package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONParser;

/**
 * Abstract super-class for data import response overlay type providing convenience methods.
 */
class AbstractDataImportResponse extends JavaScriptObject {

    private static final Logger logger = Logger.getLogger(AbstractDataImportResponse.class.getName());

    /**
     * Extracts {@link ErrorMessage}s contained in this {@link JavaScriptObject JavaScript object}'s <i>errors</i>
     * field.
     * 
     * @return the {@link List} of contained {@link ErrorMessage}s or an empty list if the <i>errors</i> field is
     *         <code>undefined</code> or empty
     * 
     * @see #getJsObjectList(String)
     */
    public final List<ErrorMessage> getErrors() {
        return getJsObjectList("errors");
    }

    /**
     * Checks if this {@link JavaScriptObject JavaScript object} contained errors.
     * 
     * @return <code>true</code> if the {@link #getErrors() error list} is neither <code>null</code> nor empty,
     *         <code>false</code> otherwise
     * @see #getErrors()
     */
    public final boolean hasErrors() {
        final List<ErrorMessage> errors = getErrors();
        return errors != null && !errors.isEmpty();
    }

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
     * Extracts the field with the provided name representing a {@link JsArrayString JavaScript array of strings} from
     * this {@link JavaScriptObject JavaScript object} and maps the contained {@link String string} elements into a
     * {@link List list} by keeping their order.
     * 
     * @param fieldName
     *            the {@String name} of the field representing a {@link JsArrayString JavaScript array of strings}
     * @return the {@link List list} containing {@link String string} elements or an empty list if the field is
     *         <code>undefined</code> or empty
     */
    protected final List<String> getStringList(final String fieldName) {
        final JsArrayString array = arrayString(fieldName);
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        } else {
            final List<String> list = new ArrayList<>(array.length());
            for (int index = 0; index < array.length(); index++) {
                list.add(array.get(index));
            }
            return list;
        }
    }

    /**
     * Extracts the field with the provided name representing a {@link JsArray JavaScript array} containing any type of
     * {@link JavaScriptObject JavaScript object}s from this {@link JavaScriptObject JavaScript object} and maps the
     * contained elements into a {@link List list} of the same type by keeping their order.
     * 
     * @param fieldName
     *            the {@String name} of the field representing a {@link JsArray JavaScript array} containing any type of
     *            {@link JavaScriptObject JavaScript object}s
     * @return the {@link List list} containing {@link String string} elements or an empty list if the field is
     *         <code>undefined</code> or empty
     */
    protected final <T extends JavaScriptObject> List<T> getJsObjectList(final String fieldName) {
        final JsArray<T> array = arrayJsObject(fieldName);
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        } else {
            final List<T> list = new ArrayList<>(array.length());
            for (int index = 0; index < array.length(); index++) {
                list.add(array.get(index));
            }
            return list;
        }
    }

    private final native <T extends JavaScriptObject> JsArray<T> arrayJsObject(String fieldName) /*-{
        return this[fieldName];
    }-*/;
    
    private final native JsArrayString arrayString(String fieldName) /*-{
        return this[fieldName];
    }-*/;

    /**
     * Extracts the field with the provided name representing a {@link String string} from this {@link JavaScriptObject
     * JavaScript object}.
     * 
     * @param fieldName
     *            the {@String name} of the field representing a {@link String string}
     * @return the {@link String string} or <code>null</code> if the field is <code>undefined</code>
     */
    protected final native String getString(String fieldName) /*-{
        return this[fieldName];
    }-*/;

    static class ErrorMessage extends JavaScriptObject {

        protected ErrorMessage() {
        }

        public final native String getExUUID() /*-{
            return this.exUUID;
        }-*/;

        public final native String getFilename() /*-{
            return this.filename;
        }-*/;

        public final native String getRequestedImporter() /*-{
            return this.requestedImporter;
        }-*/;

        public final native String getClassName() /*-{
            return this.className;
        }-*/;

        public final native String getMessage() /*-{
            return this.message;
        }-*/;
    }
}
