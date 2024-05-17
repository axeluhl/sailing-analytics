package com.sap.sse.common.fileupload;

import com.sap.sse.common.Base64Utils;

/**
 * Helper class used in conjunction with {@code AbstractFileUploadServlet} and its sub-classes which assume to be
 * servlets targeted by file upload forms, returning a JSON payload embedded in an HTML document's body. This class
 * has static helper methods that allow the servlet to {@link #getHtmlWithEmbeddedJsonContent(String) embed/encode}
 * and the client to {@link #getApplicationJsonContentFromHtml(String) extract/decode} the JSON payload
 * embedded in the servlet response's HTML document.
 * <p>
 * 
 * For file uploads through {@link FormPanel} elements, if the response is a JSON message (content type
 * {@code application/json}) then the way the {@link FormPanel} intercepts this response (namely through an auxiliary
 * {@code iframe} element into which the response is loaded) the content may get wrapped by a combination of {@code <pre>}
 * and {@code </pre>} elements. In newer Chrome versions as of around 2024-04-15, additional {@code <div>} elements may be found after
 * the closing {@code </pre>} tag. These elements persist through the {@link SubmitCompleteEvent#getResults()} method,
 * and hence must be stripped off before trying to parse the results as JSON. This is what
 * {@link #getApplicationJsonContentFromHtml(String)} does.
 * <p>
 * 
 * The background is described also in bugs 5992 and bug 5127 (especially comment #44).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FileUploadUtil {
    private final static String EMBEDDING_TAG = "div";
    
    // use an all lower-case attribute name because along the channel, HMTL content may be normalized
    private final static String PAYLOAD_ATTRIBUTE_NAME = "jsonpayloadasbase64";
    
    /**
     * Decodes a JSON {@link String} from an HTML document's {@code body} element that has
     * been produced from such a JSON string using the {@link #getHtmlWithEmbeddedJsonContent(String)}
     * method.
     */
    public static String getApplicationJsonContentFromHtml(final String resultHtml) {
        final String base64EncodedJsonPayload = resultHtml.replaceFirst("^.*<"+EMBEDDING_TAG+"  *"+PAYLOAD_ATTRIBUTE_NAME+"=\"([^\"]*)\"></"+EMBEDDING_TAG+">.*$", "$1");
        return new String(Base64Utils.fromBase64(base64EncodedJsonPayload));
    }
    
    /**
     * Embeds a JSON payload {@link String} in an HTML {@code body} element such that the resulting document is robust
     * against browser normalization, such as case changes in element and attribute names, or document adjustments for
     * display purposes. The JSON payload can be extracted from the resulting HTML string using the
     * {@link #getApplicationJsonContentFromHtml(String)} method.
     * <p>
     * 
     * In particular, the following expression is always {@code true} for any non-{@code null} {@link String}
     * {@code json}:
     * <p>
     * 
     * {@code json.equals(}{@link #getApplicationJsonContentFromHtml(String)
     * getApplicationJsonContentFromHtml(}{@link #getHtmlWithEmbeddedJsonContent(String)
     * getHtmlWithEmbeddedJsonContent(json)))}
     * <p>
     * 
     * Servlets using this method shall declare their response content type as "text/html" so that at most HTML
     * normalization would take place en-route.
     * 
     * @param json
     *            a non-{@code null} {@link String} that will be encoded safely into an HTML document body's contents so
     *            that it survives normalization and potentially enclosing into other tags and the pre-pending and
     *            appending of other tags to the content.
     * @return an HTML string whose top-level element is a {@code body} element containing the safely-encoded payload
     *         which can be decoded again using the {@link #getApplicationJsonContentFromHtml(String)} method.
     */
    public static String getHtmlWithEmbeddedJsonContent(final String json) {
        return "<body><"+EMBEDDING_TAG+" "+PAYLOAD_ATTRIBUTE_NAME+"=\""+Base64Utils.toBase64(json.getBytes())+"\"></"+EMBEDDING_TAG+"></body>";
    }
}
