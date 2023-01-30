package com.sap.sse.gwt.client.fileupload;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

public class FileUploadUtil {
    /**
     * For file uploads through {@link FormPanel} elements, if the response is a JSON message (content type
     * {@code application/json}) then the way the {@link FormPanel} intercepts this response (namely through
     * an auxiliary {@code iframe} element into which the response is loaded) the content may get wrapped by
     * a combination of {@code <pre>} and {@code </pre>} elements. These elements persist through the
     * {@link SubmitCompleteEvent#getResults()} method, and hence must be stripped off before trying to
     * parse the results as JSON. This is that this method does.<p>
     * 
     * Conversely, trying to use {@code text/html} as content encoding leads some
     * browsers---especially on mobile devices---to do ugly things to the content returned, such as replacing digit
     * sequences by a corresponding {@code <a>} element that allows the user to dial that number with the phone app...
     */
    public static String getApplicationJsonContent(SubmitCompleteEvent submitResultWithApplicationJsonContent) {
        return submitResultWithApplicationJsonContent.getResults().replaceFirst("<pre[^>]*>(.*)</pre>", "$1");
    }
}
