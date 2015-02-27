package com.sap.sse.gwt.adminconsole;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.json.client.JSONValue;

public interface StringMessages extends Messages {
    String upload();

    String remove();

    String removeResult(JSONValue jsonValue, JSONValue jsonValue2);

    String uploadSuccessful();
}
