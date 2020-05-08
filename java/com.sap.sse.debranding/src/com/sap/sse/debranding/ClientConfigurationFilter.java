package com.sap.sse.debranding;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
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
 * Use ${[variable name]} to get strings replaced within static pages. No escape syntax is currently available. All occurrences of the variables listed below
 * that are found in the document will be replaced. The following variables are available at the moment:
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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final boolean deBrandingActive = Boolean.valueOf(System.getProperty(DEBRANDING_PROPERTY_NAME, "false"));
        HttpServletResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) response,
                createReplacementMap(deBrandingActive));
        chain.doFilter(request, wrappedResponse);
        // String body = wrappedResponse.toString();
        // String replaced = new String(body);
        // for (Map.Entry<String, String> item : createReplacementMap(deBrandingActive).entrySet()) {
        // replaced = replaced.replace("${" + item.getKey() + "}", item.getValue());
        // }
        // response.getWriter().write(replaced);
    }

    @Override
    public void destroy() {
        // intentionally left blank
    }

    
    private static class ContinuousReplacer {
        StringBuffer buffer;
        Consumer<byte[]> output;
        Map<String, String> replacementMap;
        int bufferSize;
        
        public ContinuousReplacer(Map<String,String> replacementMap, Consumer<byte[]> output) {
            this.replacementMap = replacementMap;
            this.output = output;
            int maxKeyLength = this.replacementMap.keySet().stream().map(String::length)
                    .max((a, b) -> Integer.compare(a, b)).orElse(0);
            this.bufferSize = maxKeyLength == 0 ? 0 : maxKeyLength + 3;
            buffer = new StringBuffer();
        }
        
        public void push(String s) throws IOException {
            buffer.append(s);
            if (buffer.length()==bufferSize) {
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
                    buffer.delete(0, idxClosing);
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
        return map;
    }
    
    private static class CharResponseWrapper extends HttpServletResponseWrapper {
        
        private static final class MyWriter extends FilterWriter {
            private MyWriter(Writer out, Map<String,String> replacementMap) {
                super(out);
                new ContinuousReplacer(replacementMap, bytes -> out.write(new String(bytes)));
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                String s = new String(cbuf,off,len);
                super.write(cbuf, off, len);
            }

            @Override
            public void write(int c) throws IOException {
                // TODO Auto-generated method stub
                super.write(c);
            }

            @Override
            public void write(String str, int off, int len) throws IOException {
                // TODO Auto-generated method stub
                super.write(str, off, len);
            }

            @Override
            public Writer append(char arg0) throws IOException {
                // TODO Auto-generated method stub
                return super.append(arg0);
            }

            @Override
            public Writer append(CharSequence arg0, int arg1, int arg2) throws IOException {
                // TODO Auto-generated method stub
                return super.append(arg0, arg1, arg2);
            }

            @Override
            public Writer append(CharSequence arg0) throws IOException {
                // TODO Auto-generated method stub
                return super.append(arg0);
            }

            @Override
            public void write(char[] arg0) throws IOException {
                // TODO Auto-generated method stub
                super.write(arg0);
            }

            @Override
            public void write(String arg0) throws IOException {
                // TODO Auto-generated method stub
                super.write(arg0);
            }
        }

        private static final class MyServletOutputStream extends ServletOutputStream {
            private final ServletOutputStream wrappedOutputStream;
            private final ContinuousReplacer replacer;

            private MyServletOutputStream(ServletOutputStream wrappedOutputStream,Map<String,String> replacementMap) {
                this.wrappedOutputStream = wrappedOutputStream;
                this.replacer = new ContinuousReplacer(replacementMap, bytes -> wrappedOutputStream.write(bytes));
            }

            @Override
            public void write(int octet) throws IOException {
                // System.out.print();
                String current = new String(new char[] { (char) octet });
                replacer.push(current);
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // disable asynchronous writing, not needed for static pages
                throw new IllegalStateException("not supported by ClientConfigurationFilter");
            }

            @Override
            public boolean isReady() {
                return wrappedOutputStream.isReady();
            }
        }

        private Map<String, String> replacementMap;
        
        public CharResponseWrapper(HttpServletResponse response, Map<String,String> replacementMap) {
            super(response);
            this.replacementMap = replacementMap;
            response.setBufferSize(0); // prevent buffering here to not need to think about it later
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            FilterWriter filter = new MyWriter(super.getWriter(), replacementMap);
            return new PrintWriter(filter);
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
            return 0;
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
            final ServletOutputStream wrappedOutputStream = super.getOutputStream();
            return new MyServletOutputStream(wrappedOutputStream, replacementMap);
        }
    }
}
