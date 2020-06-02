package com.sap.sse.debranding;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Use ${[variable name]} to get strings replaced within static pages. No escape syntax is currently available. All
 * occurrences of the variables listed below that are found in the document will be replaced. The following variables
 * are available at the moment:
 * <table border="1">
 * <tr>
 * <th>Variablename</th>
 * <th>branded value</th>
 * <th>debranded/whitelabeled</th>
 * </tr>
 * <tr>
 * <td>"SAP"</td>
 * <td>"SAP&nbsp;"</td>
 * <td>""</td>
 * </tr>
 * <tr>
 * <td>"debrandingActive"</td>
 * <td>"false"</td>
 * <td>"true"</td>
 * </tr>
 * <tr>
 * <td>"whitelabeled"</td>
 * <td>""</td>
 * <td>"-whitelabeled"</td>
 * </tr>
 * </table>
 * <p>
 *
 * Register as a filter for all the URLs that produce such static pages that you'd like to run replacements on. Example
 * registration in a {@code web.xml} configuration file:
 * 
 * <pre>
 *   &lt;filter&gt;
 *       &lt;display-name&gt;ClientConfigurationFilter&lt;/display-name&gt;
 *       &lt;filter-name&gt;ClientConfigurationFilter&lt;/filter-name&gt;
 *       &lt;filter-class&gt;com.sap.sse.debranding.ClientConfigurationFilter&lt;/filter-class&gt;
 *   &lt;/filter&gt;
 *   &lt;filter-mapping&gt;
 *       &lt;filter-name&gt;ClientConfigurationFilter&lt;/filter-name&gt;
 *       &lt;url-pattern&gt;*.html&lt;/url-pattern&gt;
 *   &lt;/filter-mapping&gt;
 * </pre>
 * <p>
 * 
 * 
 * @see com.sap.sailing.server.gateway.test.support.WhitelabelSwitchServlet
 * @author Georg Herdt
 *
 */
public class ClientConfigurationFilter implements Filter {

    public static final String DEBRANDING_PROPERTY_NAME = "com.sap.sse.debranding";
    public static final String CLIENT_CONFIGURATION_FILTER_MAX_BUFFER = "com.sap.sse.clientconfiguration.maxbuffer";
    private static int MAX_REPLACEMENT_BUFFER = Integer
            .valueOf(System.getProperty(CLIENT_CONFIGURATION_FILTER_MAX_BUFFER, "1000000"));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final boolean deBrandingActive = Boolean.valueOf(System.getProperty(DEBRANDING_PROPERTY_NAME, "false"));
        CharResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) response,
                createReplacementMap(deBrandingActive));
        chain.doFilter(request, wrappedResponse);
        wrappedResponse.replaceAndWriteToUnderlying(wrappedResponse.getCharacterEncoding());
    }

    @Override
    public void destroy() {
        // intentionally left blank
    }

    private Map<String, String> createReplacementMap(boolean deBrandingActive) {
        final Map<String, String> map = new HashMap<>();
        final String title;
        final String whitelabeled;
        if (deBrandingActive) {
            title = "";
            whitelabeled = "-whitelabeled";
        } else {
            title = "SAP ";
            whitelabeled = "";
        }
        map.put("SAP", title);
        map.put("debrandingActive", Boolean.toString(deBrandingActive));
        map.put("whitelabeled", whitelabeled);
//        cachedMap = map;
        return map;
    }

    /**
     * Replacement logic is implemented here.
     * 
     * @author Georg Herdt
     *
     */
    private static class ContinuousReplacer {
        StringBuffer buffer;
        Consumer<byte[]> output;
        Map<String, String> replacementMap;
        int bufferSize;

        /**
         * 
         * @param replacementMap
         *            a map containing the key value mappings that will be replaced
         * @param output
         *            a consumer that accepts the replaced content. Allows abstraction from writer or stream oriented
         *            processing.
         */
        public ContinuousReplacer(Map<String, String> replacementMap, Consumer<byte[]> output) {
            this.replacementMap = replacementMap;
            this.output = output;
            int maxKeyLength = this.replacementMap.keySet().stream().map(String::length)
                    .max((a, b) -> Integer.compare(a, b)).orElse(0);
            this.bufferSize = maxKeyLength == 0 ? 0 : maxKeyLength + 3;
            buffer = new StringBuffer();
        }

        public void push(char character) throws IOException {
            buffer.append(character);
            // wait until buffer is filled up
            if (buffer.length() == bufferSize) {
                int idxClosing;
                if (buffer.substring(0, 2).equals("${") && (idxClosing = buffer.indexOf("}")) != -1) {
                    String replacement;
                    String key = buffer.substring(2, idxClosing);
                    if ((replacement = replacementMap.get(key)) != null) {
                        output.accept(replacement.getBytes());
                    } else {
                        output.accept("${".getBytes());
                        output.accept(key.getBytes());
                        output.accept("}".getBytes());
                    }
                    buffer.delete(0, idxClosing + 1);
                } else {
                    output.accept(buffer.substring(0, 1).getBytes());
                    buffer.deleteCharAt(0);
                }
            }
        }

        private interface Consumer<T> {
            void accept(T t) throws IOException;
        }
    }

    /**
     * Keeps track of bytes buffered during request processing.
     * 
     * @author Georg Herdt
     *
     */
    private static final class BufferingWriter extends FilterWriter {
        int bytesWritten = 0;

        public BufferingWriter(Writer out) {
            super(out);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            super.write(cbuf, off, len);
            count(len);
        }

        @Override
        public void write(int c) throws IOException {
            super.write(c);
            count(1);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            super.write(str, off, len);
            count(len);
        }

        private void count(int bytesWritten) {
            this.bytesWritten += bytesWritten;
            if (this.bytesWritten > MAX_REPLACEMENT_BUFFER) {
                throw new IllegalStateException("buffersize exceeded " + MAX_REPLACEMENT_BUFFER);
            }
        }
    }

    /**
     * Keeps track of bytes buffered during request processing.
     * 
     * @author Georg Herdt
     *
     */
    private static final class BufferingServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream buffer;

        private BufferingServletOutputStream() {
            this.buffer = new ByteArrayOutputStream();
        }

        @Override
        public boolean isReady() {
            // always ready to write into buffer
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // intentionally left blank, no asynchronous writing supported
            throw new IllegalStateException("async not supported");
        }

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
            if (buffer.size() > MAX_REPLACEMENT_BUFFER) {
                throw new IllegalStateException("buffersize exceeded " + MAX_REPLACEMENT_BUFFER);
            }
        }

        public String getBufferedText(Charset charset) throws UnsupportedEncodingException {
            return buffer.toString(charset.name());
        }
    }

    /**
     * Wraps response and buffers for later replacement.
     * 
     * @author Georg Herdt
     *
     */
    private static class CharResponseWrapper extends HttpServletResponseWrapper {

        private Map<String, String> replacementMap;

        private BufferingServletOutputStream bufferedStream;

        private FilterWriter bufferedWriter;

        public CharResponseWrapper(HttpServletResponse response, Map<String, String> replacementMap) {
            super(response);
            this.replacementMap = replacementMap;
            response.setBufferSize(0); // prevent buffering here to not need to think about it later
        }

        public void replaceAndWriteToUnderlying(String encoding) throws IOException {
            String text;
            Charset charset;
            try {
                charset = Charset.forName(encoding);
            } catch (IllegalArgumentException e) {
                // default charset as specified by servlet api
                charset = Charset.forName("ISO-8859-1"); 
            }
            if (bufferedStream != null) {
                text = bufferedStream.getBufferedText(charset);
            } else if (bufferedWriter != null) {
                text = bufferedWriter.toString();
            } else {
                text = null;
            }
            if (text != null) {
                ContinuousReplacer replacer = new ContinuousReplacer(replacementMap,
                        b -> this.getResponse().getOutputStream().write(b));
                for (char c : text.toCharArray()) {
                    replacer.push(c);
                }
            }
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (bufferedStream != null) {
                throw new IllegalStateException("getOutputStream already called");
            }
            this.bufferedWriter = new BufferingWriter(new CharArrayWriter());
            return new PrintWriter(this.bufferedWriter);
        }

        @Override
        public void setResponse(ServletResponse response) {
            super.setResponse(response);
        }

        @Override
        public void setBufferSize(int size) {
            super.setBufferSize(0);
        }

        @Override
        public int getBufferSize() {
            return 0; // not supporting buffering here see API doc
        }

        @Override
        public void flushBuffer() throws IOException {
            // intentionally left blank, not supporting buffering here
        }

        @Override
        public void reset() {
            throw new IllegalStateException("not supported by " + this.getClass().getCanonicalName());
        }

        @Override
        public void resetBuffer() {
            // intentionally left blank, not supporting buffering here
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (bufferedWriter != null) {
                throw new IllegalStateException("getWriter already called");
            }
            this.bufferedStream = new BufferingServletOutputStream();
            return bufferedStream;
        }
    }
}
