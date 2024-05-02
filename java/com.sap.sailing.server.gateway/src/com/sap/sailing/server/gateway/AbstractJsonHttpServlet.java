package com.sap.sailing.server.gateway;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONAware;

import com.sap.sse.common.fileupload.FileUploadUtil;

/**
 * Base servlet for JSON exports. If implementing subclasses use {@link #setJsonResponseHeader(HttpServletResponse)},
 * they are assumed to write their response to the servlet response's {@link HttpServletResponse#getOutputStream()
 * output stream} directly. Alternatively, e.g., when used as the target of a file upload form that technically expects
 * HTML output, the output can be set to {@code text/html}, and a special encoding of a JSON response will be used so it
 * can be embedded robustly in an HTML document's {@code body} tag and be extracted again by the client receiving the
 * event about submission completion.
 * 
 * @author Frank Mittag
 * @author Axel Uhl
 */
@SuppressWarnings("serial")
public abstract class AbstractJsonHttpServlet extends SailingServerHttpServlet {
    /**
     * By invoking this method you declare that you will write the JSON document
     * straight to the output stream.
     */
    protected void setJsonResponseHeader(HttpServletResponse resp) {
        setBasicPropertiesInResponseHeader(resp);
        resp.setContentType("application/json");
    }

    /**
     * By invoking this method you declare that you assume being the target of a form submission
     * and will encode your JSON into an HTML document's {@code body} tag, using the
     * {@link FileUploadUtil} class.
     */
    protected void setJsonEncodedInHtmlResponseHeader(HttpServletResponse resp) {
        setBasicPropertiesInResponseHeader(resp);
        resp.setContentType("text/html");
    }

    private void setBasicPropertiesInResponseHeader(HttpServletResponse resp) {
        // to allow access to the json document directly from a client side javascript
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setCharacterEncoding("UTF-8");
    }

    /**
     * Encodes the stringified {@code json} object into an HTML document's {@code body} element, writing the output into
     * {@code resp}'s {@link HttpServletResponse#getWriter() writer}. The encoding is done using
     * {@link FileUploadUtil#getHtmlWithEmbeddedJsonContent(String)} which the client can
     * then also use to {@link FileUploadUtil#getEmbeddedJsonContentFromHtml(String, java.util.function.Function)
     * extract} the pure JSON string from the HTML carrier document again.
     */
    protected void writeJsonIntoHtmlResponse(HttpServletResponse resp, JSONAware json) throws IOException {
        resp.getWriter().write(FileUploadUtil.getHtmlWithEmbeddedJsonContent(json.toJSONString()));
    }
}
