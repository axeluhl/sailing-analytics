package com.sap.sse.gwt.adminconsole;

import com.google.gwt.i18n.client.Messages;

public interface StringMessages extends Messages {
    String upload();

    String remove();

    String removeResult(String status, String message);

    String uploadSuccessful();

    String fileUploadResult(String status, String message);
}
