package com.sap.sse.gwt.client.fileupload;

public class FileUploadUtil {
    public static String removeSurroundingPreElement(String applicationJsonContent) {
        return applicationJsonContent.replaceFirst("<pre[^>]*>(.*)</pre>", "$1");
    }
}
